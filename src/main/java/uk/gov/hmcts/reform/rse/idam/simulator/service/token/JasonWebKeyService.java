package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKey;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKeySet;

import java.util.Collections;

@Component
public class JasonWebKeyService {

    static String alg = "RS256";
    static String kty = "RSA";
    static String use = "sig";
    static String kid = "1er0WRwgIOTAFojE4rC/fbeKu3I=";
    static String n = "01uAHoJeLguETwCkjNocgjmDwstRtJAJEcZbMH-m8InvZxWUEJl7Icjgb_gx_NCLkkjo7HEs0XaBTiwZyxxVN8gKym2HKEdiP3z9c2W-H0Uu7mD3a0o366mUWrgi1cg8V6X7jwex2C7j7NJV4gxqWQBlXNHRBeLjmxWe-KRHSrRO_-713jieib0r3LbZ_AoXshxYw7zo5mcvkKYv9M5QduLXEcJI6UT1YfXYUogVARIkMvjHO2cA5St1NGdBZtB4u8vvJwZSfp2aNlGCgZ4NxL9t-C6oDgNYYLJVgwh79wgwrz1i9uNRHaNal109-9sr2LJuHkw2AIPMH6bfhlgJUQ";
    static String e = "AQAB";
    static String x5c1 = "MIIDQjCCAiqgAwIBAgIQMMTtQtR0SjaiPrXYa3CUajANBgkqhkiG9w0BAQsFADAeMRwwGgYDVQQDExNDTElHZXREZWZhdWx0UG9saWN5MB4XDTIwMDMwNDEwMzAxNloXDTIxMDMwNDEwNDAxNlowHjEcMBoGA1UEAxMTQ0xJR2V0RGVmYXVsdFBvbGljeTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBANNbgB6CXi4LhE8ApIzaHII5g8LLUbSQCRHGWzB/pvCJ72cVlBCZeyHI4G/4MfzQi5JI6OxxLNF2gU4sGcscVTfICspthyhHYj98/XNlvh9FLu5g92tKN+uplFq4ItXIPFel+48Hsdgu4+zSVeIMalkAZVzR0QXi45sVnvikR0q0Tv/u9d44nom9K9y22fwKF7IcWMO86OZnL5CmL/TOUHbi1xHCSOlE9WH12FKIFQESJDL4xztnAOUrdTRnQWbQeLvL7ycGUn6dmjZRgoGeDcS/bfguqA4DWGCyVYMIe/cIMK89YvbjUR2jWpddPfvbK9iybh5MNgCDzB+m34ZYCVECAwEAAaN8MHowDgYDVR0PAQH/BAQDAgG+MAkGA1UdEwQCMAAwHQYDVR0lBBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMB8GA1UdIwQYMBaAFL09leR2dfva6lyZO4Ph75zD2wkrMB0GA1UdDgQWBBS9PZXkdnX72upcmTuD4e+cw9sJKzANBgkqhkiG9w0BAQsFAAOCAQEAd+bXaYrr+OamO3hzUsenrz88DR6TTq0YTDXLs7cjLbEPHG02YScp8rIaPgAyKNtUV/UY3sak6cF1SqrTI5hK9YM/jIh57BenFZLxEZJk29/yXDstPoQXTKpPpAXaDh2ANf+p4d9H+pZ2zTAyspBzGH5ZnFIWJPuABtpmcTk4M5C1PjqvgqzGqpilkUHBX6XE2JyIGpvm9THQ6rodyu5EDKYy64YRNjoauZMpwA1ylINjwEPv+LG+19s8+REyoq6ukGiYzLT4H92ONF7fngLifSnk4nTh037tpo+dj0+RfWxAXScQcvXI5LXvWyIu1LTGK+PRILpgJ2flXUGwQz4VvQ==";
    static String x5t = "TnQ48fb5i968JO_zOG6XME3GG8E";

    public JsonWebKeySet getJwkConfigSet() {
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet();
        JsonWebKey jsonWebKey = new JsonWebKey();
        jsonWebKey.setAlg(alg);
        jsonWebKey.setK(kty);
        jsonWebKey.setUse(use);
        jsonWebKey.setKid(kid);
        jsonWebKey.setN(n);
        jsonWebKey.setE(e);
        jsonWebKey.setX5c(Collections.singletonList(x5c1));
        jsonWebKey.setX5t(x5t);
        jsonWebKeySet.setKeys(Collections.singletonList(jsonWebKey));
        return jsonWebKeySet;
    }
}


