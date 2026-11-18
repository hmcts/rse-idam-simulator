package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKey;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKeySet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;

@Component
public class JsonWebKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonWebKeyService.class);

    private static final String DEFAULT_SIGNING_KEY_RESOURCE = "simulator-signing-key.json";
    private static final int MINIMUM_KEY_BITS = 2048;

    private final RSAKey signingRsaKey;
    private final JsonWebKeySet publicKeySet;

    public JsonWebKeyService(@Value("${simulator.jwt.signing-key:}") String signingKey) {
        signingRsaKey = parseSigningKey(StringUtils.hasText(signingKey) ? signingKey : readDefaultSigningKey());
        publicKeySet = toPublicKeySet(signingRsaKey);
        LOG.info("Simulator signing key loaded with key id {}", signingRsaKey.getKeyID());
    }

    private static JsonWebKeySet toPublicKeySet(RSAKey rsaKey) {
        JsonWebKey jsonWebKey = new JsonWebKey();
        jsonWebKey.setAlg("RS256");
        jsonWebKey.setKty("RSA");
        jsonWebKey.setUse("sig");
        jsonWebKey.setKid(rsaKey.getKeyID());
        jsonWebKey.setN(rsaKey.getModulus().toString());
        jsonWebKey.setE(rsaKey.getPublicExponent().toString());
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet();
        jsonWebKeySet.setKeys(Collections.singletonList(jsonWebKey));
        return jsonWebKeySet;
    }

    public JsonWebKeySet getJwkConfigSet() {
        return publicKeySet;
    }

    public RSAKey  getRsaKey() {
        return signingRsaKey;
    }

    private static RSAKey parseSigningKey(String signingKey) {
        RSAKey rsaKey;
        try {
            rsaKey = RSAKey.parse(signingKey);
        } catch (ParseException | IllegalArgumentException exception) {
            throw new TokenGenerationException("The simulator signing key is not a valid RSA JWK");
        }
        if (rsaKey.getPrivateExponent() == null) {
            throw new TokenGenerationException("The simulator signing key has no private part");
        }
        if (!StringUtils.hasText(rsaKey.getKeyID())) {
            throw new TokenGenerationException("The simulator signing key has no key id");
        }
        if (rsaKey.getModulus().decodeToBigInteger().bitLength() < MINIMUM_KEY_BITS) {
            throw new TokenGenerationException("The simulator signing key must be at least 2048 bits");
        }
        if (!canSignAndVerify(rsaKey)) {
            throw new TokenGenerationException("The simulator signing key cannot sign and verify");
        }
        return rsaKey;
    }

    private static boolean canSignAndVerify(RSAKey rsaKey) {
        try {
            JWSObject signingCheck = new JWSObject(new JWSHeader(JWSAlgorithm.RS256), new Payload("signing-check"));
            signingCheck.sign(new RSASSASigner(rsaKey));
            return signingCheck.verify(new RSASSAVerifier(rsaKey.toPublicJWK()));
        } catch (JOSEException | RuntimeException exception) {
            return false;
        }
    }

    private static String readDefaultSigningKey() {
        ClassPathResource resource =
            new ClassPathResource(DEFAULT_SIGNING_KEY_RESOURCE, JsonWebKeyService.class.getClassLoader());
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new TokenGenerationException("Impossible to read " + DEFAULT_SIGNING_KEY_RESOURCE);
        }
    }
}


