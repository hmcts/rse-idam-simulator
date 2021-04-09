package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
@RunWith(MockitoJUnitRunner.class)
public class JwTokenGeneratorTest {

    @Mock
    JsonWebKeyService jsonWebKeyService;

    @InjectMocks
    JwTTokenGeneratorService jwTokenGeneratorService;

    @Test
    public void generatorShouldReturnADifferentTokenEachTime() throws JOSEException {

        RSAKey rsaKey = generateRsaKey();
        when(jsonWebKeyService.getRsaKey()).thenReturn(rsaKey);

        String issuer = "issued";
        long ttl = 900;
        String userName = "username";
        String serviceId = "myserviceId";
        String grantType = "password";
        String token1 = jwTokenGeneratorService.generateToken(issuer, ttl, userName, serviceId, grantType);
        String token2 = jwTokenGeneratorService.generateToken(issuer, ttl, userName, serviceId, grantType);

        assertThat(token1.length() > 200);
        assertThat(token2.length() > 200);

        assertNotSame(token1, token2);
    }

    private RSAKey generateRsaKey() throws JOSEException {
        return new RSAKeyGenerator(2048)
            .keyID(Long.toString(System.nanoTime()))
            .generate();
    }

}
