package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

@SuppressWarnings("PMD.NonThreadSafeSingleton")
public final class KeyGenUtil {

    private static final String KEY_ID = "23456789";

    private KeyGenUtil() {
    }

    public static RSAKey getRsaJwk() {
        try {
            return new RSAKeyGenerator(2048)
                .keyID(KEY_ID)
                .generate();
        } catch (JOSEException josee) {
            throw new TokenGenerationException("Impossible to generate RSA Key", josee);
        }
    }

}
