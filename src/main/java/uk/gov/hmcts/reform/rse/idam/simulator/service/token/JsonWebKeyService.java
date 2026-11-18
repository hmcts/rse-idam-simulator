package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    private static final String DEFAULT_SIGNING_KEY_RESOURCE = "/simulator-signing-key.json";

    private final RSAKey signingRsaKey;
    private final JsonWebKeySet generatedKeySet;

    public JsonWebKeyService(@Value("${simulator.jwt.signing-key:}") String signingKey) {
        signingRsaKey = parseSigningKey(StringUtils.hasText(signingKey) ? signingKey : readDefaultSigningKey());
        generatedKeySet = generateNewJwkConfigSet(signingRsaKey);
    }

    /**
     * Generate a new JsonWebKeySet without x5c,X5t. x5c,X5t are related to X.509 certificate chain.
     */
    public final JsonWebKeySet generateNewJwkConfigSet(RSAKey rsaKey) {
        JsonWebKey jsonWebKey = new JsonWebKey();
        jsonWebKey.setAlg("RS256");
        jsonWebKey.setKty("RSA");
        jsonWebKey.setUse("sig");
        jsonWebKey.setKid(rsaKey.getKeyID());
        jsonWebKey.setN(rsaKey.getModulus().toString());
        jsonWebKey.setE(rsaKey.getPublicExponent().toString());
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet();
        jsonWebKeySet.setKeys(Collections.singletonList(jsonWebKey));
        LOG.info("New JsonWebKeySet Generated {}", jsonWebKeySet.toString());
        return jsonWebKeySet;
    }

    public JsonWebKeySet getJwkConfigSet() {
        return generatedKeySet;
    }

    public RSAKey  getRsaKey() {
        return signingRsaKey;
    }

    /**
     * The parse failure is deliberately not chained: its message can quote the key material.
     */
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
        return rsaKey;
    }

    private static String readDefaultSigningKey() {
        try (InputStream resource = JsonWebKeyService.class.getResourceAsStream(DEFAULT_SIGNING_KEY_RESOURCE)) {
            if (resource == null) {
                throw new TokenGenerationException("Missing resource " + DEFAULT_SIGNING_KEY_RESOURCE);
            }
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ioException) {
            throw new TokenGenerationException("Impossible to read " + DEFAULT_SIGNING_KEY_RESOURCE, ioException);
        }
    }
}


