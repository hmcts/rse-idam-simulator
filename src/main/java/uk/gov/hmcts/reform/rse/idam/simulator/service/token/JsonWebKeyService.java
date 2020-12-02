package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKey;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKeySet;

import java.util.Collections;

@Component
public class JsonWebKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonWebKeyService.class);

    private final RSAKey generatedRsaKey;
    private final JsonWebKeySet generatedKeySet;

    public JsonWebKeyService() {
        generatedRsaKey = generateNewRsaKey();
        generatedKeySet = generateNewJwkConfigSet(generatedRsaKey);
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

    public final RSAKey generateNewRsaKey() {
        try {
            synchronized (this) {
                RSAKey rsaJwk = new RSAKeyGenerator(2048)
                    .keyID(Long.toString(System.nanoTime()))
                    .generate();

                LOG.info("New Idam Simulator RSA Key generated: {}", rsaJwk.toString());
                return rsaJwk;
            }
        } catch (JOSEException josee) {
            throw new TokenGenerationException("Impossible to generate RSA Key", josee);
        }
    }

    public JsonWebKeySet getJwkConfigSet() {
        return generatedKeySet;
    }

    public RSAKey  getRsaKey() {
        return generatedRsaKey;
    }
}


