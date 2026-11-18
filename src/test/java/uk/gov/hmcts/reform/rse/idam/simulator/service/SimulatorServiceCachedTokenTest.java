package uk.gov.hmcts.reform.rse.idam.simulator.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.PersistentStorageService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.UserService;

import java.nio.file.Path;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class SimulatorServiceCachedTokenTest {

    private static final String EMAIL = "user@example.com";
    private static final String USER_ID = "user-id";
    private static final String CLIENT_ID = "client";
    private static final String GRANT_TYPE = "password";
    private static final long EXPIRATION_MS = 3_600_000;

    private final JsonWebKeyService jsonWebKeyService = new JsonWebKeyService("");

    @TempDir
    static Path tempDir;

    static Stream<String> stores() {
        return Stream.of("memory", "persistent");
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldReuseAnUnexpiredTokenSignedByTheCurrentKey(String store) {
        SimulatorService service = simulatorService(store);
        String token = service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE);
        service.updateTokenInUser(EMAIL, token);

        assertEquals(token, service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldRegenerateATokenSignedByAnotherKey(String store) throws JOSEException, ParseException {
        SimulatorService service = simulatorService(store);
        RSAKey otherKey = new RSAKeyGenerator(2048).keyID(jsonWebKeyService.getRsaKey().getKeyID()).generate();
        String foreignToken = signedToken(otherKey, Instant.now().plus(Duration.ofHours(1)));
        service.updateTokenInUser(EMAIL, foreignToken);

        String token = service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE);

        assertNotEquals(foreignToken, token);
        assertVerifiesAndIsUnexpired(token);
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldRegenerateAnExpiredTokenEvenWhenItWasJustCached(String store)
        throws JOSEException, ParseException {
        SimulatorService service = simulatorService(store);
        String expiredToken = signedToken(jsonWebKeyService.getRsaKey(), Instant.now().minus(Duration.ofMinutes(1)));
        service.updateTokenInUser(EMAIL, expiredToken);

        String token = service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE);

        assertNotEquals(expiredToken, token);
        assertVerifiesAndIsUnexpired(token);
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldRegenerateAnUnparseableToken(String store) throws JOSEException, ParseException {
        SimulatorService service = simulatorService(store);
        for (String unparseable : List.of("", "not-a-token", "a.b.c", "e30.e30.e30")) {
            service.updateTokenInUser(EMAIL, unparseable);

            assertVerifiesAndIsUnexpired(service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE));
        }
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldRegenerateATokenWithoutAnExpiry(String store) throws JOSEException, ParseException {
        SimulatorService service = simulatorService(store);
        String tokenWithoutExpiry = signedToken(jsonWebKeyService.getRsaKey(), null);
        service.updateTokenInUser(EMAIL, tokenWithoutExpiry);

        assertVerifiesAndIsUnexpired(service.generateACachedToken(EMAIL, CLIENT_ID, GRANT_TYPE));
    }

    @ParameterizedTest
    @MethodSource("stores")
    public void shouldNotLoseConcurrentChangesToTheSameUser(String store) {
        SimulatorService service = simulatorService(store);
        for (int round = 0; round < 100; round++) {
            CompletableFuture<String> codeRequest =
                CompletableFuture.supplyAsync(() -> service.geAuthCodeFromUserName(EMAIL, "nonce"));
            CompletableFuture<String> pinRequest =
                CompletableFuture.supplyAsync(() -> service.createPinDetails("Forename", "Surname").getPin());
            String code = codeRequest.join();
            String pin = pinRequest.join();

            assertThat(service.checkUserInMemoryNotEmptyByCode(code).getLastGeneratedPin()).isEqualTo(pin);
        }
    }

    private SimulatorService simulatorService(String store) {
        UserService userService = "memory".equals(store)
            ? new LiveMemoryService()
            : new PersistentStorageService(tempDir.resolve(System.nanoTime() + "-users.db").toString());
        userService.putSimObject(USER_ID, SimObject.builder()
            .id(USER_ID)
            .email(EMAIL)
            .forename("Forename")
            .surname("Surname")
            .roles(new ArrayList<>(List.of("caseworker")))
            .build());

        JwTokenGeneratorService jwTokenGenerator = new JwTokenGeneratorService();
        ReflectionTestUtils.setField(jwTokenGenerator, "jsonWebKeyService", jsonWebKeyService);

        SimulatorService service = new SimulatorService();
        ReflectionTestUtils.setField(service, "jwTokenGenerator", jwTokenGenerator);
        ReflectionTestUtils.setField(service, "userService", userService);
        ReflectionTestUtils.setField(service, "issuer", "issuer");
        ReflectionTestUtils.setField(service, "tokenExpirationMs", EXPIRATION_MS);
        return service;
    }

    private String signedToken(RSAKey rsaKey, Instant expiry) throws JOSEException {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().claim("sub", EMAIL);
        if (expiry != null) {
            claims.expirationTime(Date.from(expiry));
        }
        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(),
            claims.build()
        );
        signedJwt.sign(new RSASSASigner(rsaKey));
        return signedJwt.serialize();
    }

    private void assertVerifiesAndIsUnexpired(String token) throws JOSEException, ParseException {
        SignedJWT signedJwt = SignedJWT.parse(token);
        assertTrue(signedJwt.verify(new RSASSAVerifier(jsonWebKeyService.getRsaKey().toPublicJWK())));
        assertThat(signedJwt.getJWTClaimsSet().getExpirationTime()).isInTheFuture();
    }
}
