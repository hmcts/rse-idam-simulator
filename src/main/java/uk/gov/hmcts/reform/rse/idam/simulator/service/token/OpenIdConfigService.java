package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.OpenIdConfig;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Collections.singletonList;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals"})
@Component
public class OpenIdConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(OpenIdConfigService.class);

    public OpenIdConfig getOpenIdConfig(String baseUrl, int serverPort, String issuer) {
        OpenIdConfig openIdConfig = new OpenIdConfig();
        String basePath = baseUrl.concat(":" + serverPort);
        openIdConfig.authorizationEndpoint(basePath + "/o/authorize")
            .requestParameterSupported(true)
            .claimsParameterSupported(true)
            .scopesSupported(Arrays.asList("openid", "profile", "roles"))
            .idTokenEncryptionEncValuesSupported(Arrays.asList("A256GCM", "A256CBC-HS512"))
            .acrValuesSupported(new ArrayList<>())
            .claimsSupported(new ArrayList<>())
            .requestObjectEncryptionEncValuesSupported(Arrays.asList("A256GCM", "A256CBC-HS512"))
            .rcsRequestEncryptionAlgValuesSupported(Arrays.asList(
                "RSA-OAEP",
                "RSA-OAEP-256"
            ))
            .rcsRequestSigningAlgValuesSupported(Arrays.asList(
                "HS256",
                "ES256",
                "RS256",
                "PS256"
            ))
            .tokenEndpointAuthMethodsSupported(Arrays.asList(
                "client_secret_post",
                "private_key_jwt",
                "self_signed_tls_client_auth",
                "tls_client_auth",
                "none",
                "client_secret_basic"
            ))
            .responseTypesSupported(Arrays.asList(
                "code token id_token",
                "code",
                "code id_token",
                "device_code",
                "id_token",
                "code token",
                "token",
                "token id_token"
            ))
            .requestUriParameterSupported(true)
            .rcsResponseEncryptionAlgValuesSupported(Arrays.asList("A256GCM", "A128CBC-HS256", "A256CBC-HS512"))
            .rcsRequestEncryptionEncValuesSupported(Arrays.asList("A256GCM", "A128CBC-HS256", "A256CBC-HS512"))
            .version("3.0")
            .rcsResponseEncryptionAlgValuesSupported(Arrays.asList(
                "RSA-OAEP",
                "RSA-OAEP-256",
                "A256KW",
                "RSA1_5"
            ))
            .idTokenEncryptionAlgValuesSupported(Arrays.asList(
                "RSA-OAEP",
                "RSA-OAEP-256",
                "A256KW",
                "RSA1_5"
            ))
            .subjectTypesSupported(singletonList("public"))
            .idTokenSigningAlgValuesSupported(Arrays.asList("HS256", "RS256"))
            .requestObjectSigningAlgValuesSupported(Arrays.asList("HS256", "ES256", "RS256"))
            .requestObjectEncryptionAlgValuesSupported(Arrays.asList("RSA-OAEP", "RSA-OAEP-256", "A256KW"))
            .rcsResponseSigningAlgValuesSupported(Arrays.asList(
                "HS256",
                "ES256",
                "RS256",
                "PS256"
            ))
            .rcsResponseEncryptionEncValuesSupported(Arrays.asList("A256GCM", "A128CBC-HS256", "A256CBC-HS512"))
            .issuer(issuer)
            .tokenEndpoint(basePath + "/o/token")
            .userinfoEndpoint(basePath + "/o/userinfo")
            .jwksUri(basePath + "/o/jwks")
            .issuer(basePath)
            .endSessionEndpoint(basePath + "/o/endSession")
            .checkSessionIframe(null)
            .introspectionEndpoint(null)
            .registrationEndpoint(null);
        LOG.info("New openId Config Generated {}", openIdConfig.toString());
        return openIdConfig;
    }

}


