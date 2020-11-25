package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.util.Date;

@SuppressWarnings({"PMD.UseObjectForClearerAPI"})
@Component
public class JwTokenGeneratorService {

    public synchronized String generateToken(String issuer, long ttlMillis, String userName,
                                       String serviceId, String grantType) {

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
            .subject("RSE-Idam-Simulator")
            .issueTime(new Date())
            .issuer(issuer)
            .claim("token_type", "Bearer")
            .claim("aud", serviceId)
            .claim("sub", userName)
            .claim("grant_type", grantType)
            .claim("realm", "/hmcts")
            .claim("tokenName", "access_token");

        if (ttlMillis >= 0) {
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);
            builder.expirationTime(exp);
        }

        RSAKey rsaJwk1 = KeyGenUtil.getRsaJwk();
        RSAKey rsaJwk2 = KeyGenUtil.getRsaJwk();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaJwk1.getKeyID()).build();

        SignedJWT signedJwt = new SignedJWT(
            jwsHeader,
            builder.build()
        );
        try {
            signedJwt.sign(new RSASSASigner(rsaJwk2));
        } catch (JOSEException josee) {
            throw new TokenGenerationException("Error when signing token", josee);
        }

        return signedJwt.serialize();
    }
}
