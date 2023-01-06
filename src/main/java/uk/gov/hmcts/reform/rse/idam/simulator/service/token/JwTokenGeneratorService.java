package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;


@SuppressWarnings({"PMD.UseObjectForClearerAPI"})
@Component
public class JwTokenGeneratorService {

    private static final Logger LOG = LoggerFactory.getLogger(JsonWebKeyService.class);

    @Autowired
    JsonWebKeyService jsonWebKeyService;

    public String generateToken(String issuer, long ttlMillis, String userName,
                                String serviceId, String grantType) {

        Date authTime = new Date();

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
            .subject("RSE-Idam-Simulator")
            .jwtID(UUID.randomUUID().toString())
            .issueTime(authTime)
            .notBeforeTime(authTime)
            .issuer(issuer)
            .claim("token_type", "Bearer")
            .claim("aud", serviceId)
            .claim("sub", userName)
            .claim("grant_type", grantType)
            .claim("realm", "/hmcts")
            .claim("tokenName", "access_token")
            .claim("authGrantId", UUID.randomUUID().toString())
            .claim("auditTrackingId", UUID.randomUUID().toString())
            .claim("auth_level", 0)
            .claim("auth_time", authTime.getTime())
            .claim("scope", List.of("openid", "profile", "roles"))
            .claim("expires_in", ttlMillis / 1000);

        if (ttlMillis >= 0) {
            long expMillis = System.currentTimeMillis() + ttlMillis;
            Date exp = new Date(expMillis);
            builder.expirationTime(exp);
        }

        RSAKey rsaJwk1 = jsonWebKeyService.getRsaKey();

        JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaJwk1.getKeyID())
            .type(JOSEObjectType.JWT)
            .customParam("zip", "NONE")
            .build();

        SignedJWT signedJwt = new SignedJWT(
            jwsHeader,
            builder.build()
        );
        try {
            signedJwt.sign(new RSASSASigner(rsaJwk1));
            LOG.info("New token generated with keyid {} and signature {}", rsaJwk1.getKeyID(),
                     signedJwt.getSignature()
            );
        } catch (JOSEException josee) {
            throw new TokenGenerationException("Error when signing token", josee);
        }

        return signedJwt.serialize();
    }
}
