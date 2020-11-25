package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.jwk.RSAKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKey;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKeySet;

import java.util.Collections;

@Component
public class JasonWebKeyService {

    private static final Logger LOG = LoggerFactory.getLogger(JasonWebKeyService.class);

    static String ALGO = "RS256";
    static String KTY = "RSA";
    static String USAGE = "sig";

    private JsonWebKeySet generatedKeySet;

    private JasonWebKeyService() {
        generatedKeySet = generateNewJwkConfigSet();
    }

    /**
     * Generate a new JsonWebKeySet without x5c,X5t. x5c,X5t are related to X.509 certificate chain.
     */
    public JsonWebKeySet generateNewJwkConfigSet() {
        RSAKey rsaKey = KeyGenUtil.getRsaJwk();
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet();
        JsonWebKey jsonWebKey = new JsonWebKey();
        jsonWebKey.setAlg(ALGO);
        jsonWebKey.setK(KTY);
        jsonWebKey.setUse(USAGE);
        jsonWebKey.setKid(rsaKey.getKeyID());
        jsonWebKey.setN(rsaKey.getModulus().toString());
        jsonWebKey.setE(rsaKey.getPublicExponent().toString());
        jsonWebKeySet.setKeys(Collections.singletonList(jsonWebKey));
        LOG.info("New Idam Simulator RSA Key generated: {}", rsaKey.toString());
        LOG.info("New JsonWebKeySet Generated {}", jsonWebKeySet.toString());
        return jsonWebKeySet;
    }

    public JsonWebKeySet getJwkConfigSet() {
        return generatedKeySet;
    }
}


