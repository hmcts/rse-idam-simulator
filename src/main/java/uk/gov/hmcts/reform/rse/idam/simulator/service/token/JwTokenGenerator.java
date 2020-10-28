package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Date;

@SuppressWarnings({"PMD.UseObjectForClearerAPI"})
public final class JwTokenGenerator {

    private JwTokenGenerator() {
    }

    public static String generateToken(String issuer, long ttlMillis, String userName,
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

        SignedJWT signedJwt = new SignedJWT(
            new JWSHeader.Builder(JWSAlgorithm.RS256)
                .keyID(KeyGenUtil.getRsaJwk().getKeyID()).build(),
            builder.build()
        );
        try {
            signedJwt.sign(new RSASSASigner(KeyGenUtil.getRsaJwk()));
        } catch (JOSEException josee) {
            throw new TokenGenerationException("Error when signing token", josee);
        }

        return signedJwt.serialize();
    }
}
