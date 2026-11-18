package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKey;

import java.text.ParseException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class JsonWebKeyServiceTest {

    private static final List<String> PRIVATE_PARAMETERS = List.of("d", "p", "q", "dp", "dq", "qi");

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    public void shouldUseTheSameKeyAndKeyIdOnEveryStartByDefault(String blankSigningKey) {
        RSAKey rsaKey1 = new JsonWebKeyService(blankSigningKey).getRsaKey();
        RSAKey rsaKey2 = new JsonWebKeyService(blankSigningKey).getRsaKey();

        assertTrue(rsaKey1.isPrivate());
        assertEquals(2048, rsaKey1.size());
        assertEquals(rsaKey1, rsaKey2);
        assertEquals(rsaKey1.getKeyID(), rsaKey2.getKeyID());
    }

    @Test
    public void shouldTreatAMissingOverrideAsBlank() {
        assertEquals(new JsonWebKeyService("").getRsaKey(), new JsonWebKeyService(null).getRsaKey());
    }

    @Test
    public void shouldUseTheOverrideWhenProvided() throws JOSEException {
        RSAKey override = generateRsaKey("override-kid");

        JsonWebKeyService jsonWebKeyService = new JsonWebKeyService(override.toJSONString());

        assertEquals(override, jsonWebKeyService.getRsaKey());
        assertEquals("override-kid", jsonWebKeyService.getJwkConfigSet().getKeys().get(0).getKid());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "not-json-s3cr3t-value",
        "{\"kty\":\"RSA\",\"d\":\"s3cr3t-value\"",
        "[\"s3cr3t-value\"]",
        "{\"kty\":\"RSA\",\"kid\":\"s3cr3t-value\"}",
        "{\"kty\":\"EC\",\"kid\":\"s3cr3t-value\",\"crv\":\"P-256\",\"x\":\"AA\",\"y\":\"AA\",\"d\":\"AA\"}",
        "{\"kty\":\"RSA\",\"kid\":\"s3cr3t-value\",\"n\":\"AQAB\",\"e\":\"AQAB\",\"d\":\"AQAB\",\"p\":\"AQAB\"}"
    })
    public void shouldRejectAMalformedOverrideWithoutLeakingIt(String malformed) {
        TokenGenerationException exception = assertThrows(
            TokenGenerationException.class, () -> new JsonWebKeyService(malformed));

        assertEquals("The simulator signing key is not a valid RSA JWK", exception.getMessage());
        assertThat(exception).hasNoCause();
        assertThat(exception.toString()).doesNotContain("s3cr3t-value");
    }

    @Test
    public void shouldRejectAPublicOnlyOverrideWithoutLeakingIt() throws JOSEException {
        RSAKey publicOnly = generateRsaKey("public-only-kid").toPublicJWK();

        TokenGenerationException exception = assertThrows(
            TokenGenerationException.class, () -> new JsonWebKeyService(publicOnly.toJSONString()));

        assertEquals("The simulator signing key has no private part", exception.getMessage());
        assertThat(exception).hasNoCause();
        assertThat(exception.toString()).doesNotContain(publicOnly.getModulus().toString());
    }

    @Test
    public void shouldPublishOnlyPublicParameters() {
        JsonWebKeyService jsonWebKeyService = new JsonWebKeyService("");
        RSAKey rsaKey = jsonWebKeyService.getRsaKey();

        List<JsonWebKey> keys = jsonWebKeyService.getJwkConfigSet().getKeys();

        assertEquals(1, keys.size());
        assertEquals(rsaKey.getKeyID(), keys.get(0).getKid());
        assertEquals(rsaKey.getModulus().toString(), keys.get(0).getN());
        assertEquals(rsaKey.getPublicExponent().toString(), keys.get(0).getE());

        JsonNode published = new ObjectMapper().valueToTree(jsonWebKeyService.getJwkConfigSet()).get("keys").get(0);
        PRIVATE_PARAMETERS.forEach(parameter -> assertThat(published.has(parameter)).isFalse());
    }

    @Test
    public void tokenSignedBeforeARestartShouldVerifyAfterIt() throws JOSEException, ParseException {
        JwTokenGeneratorService jwTokenGeneratorService = new JwTokenGeneratorService();
        ReflectionTestUtils.setField(jwTokenGeneratorService, "jsonWebKeyService", new JsonWebKeyService(""));
        String token = jwTokenGeneratorService.generateToken("issuer", 900, "username", "serviceId", "password");

        RSAKey restartedKey = new JsonWebKeyService("").getRsaKey();
        SignedJWT signedJwt = SignedJWT.parse(token);

        assertEquals(restartedKey.getKeyID(), signedJwt.getHeader().getKeyID());
        assertTrue(signedJwt.verify(new RSASSAVerifier(restartedKey.toPublicJWK())));
    }

    private RSAKey generateRsaKey(String keyId) throws JOSEException {
        return new RSAKeyGenerator(2048).keyID(keyId).generate();
    }
}
