package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotSame;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class JwTokenGeneratorTest {

    @Test
    public void generatorShouldReturnADifferentTokenEachTime() {
        String issuer = "issued";
        long ttl = 900;
        String userName = "username";
        String serviceId = "myserviceId";
        String grantType = "password";
        String token1 = JwTokenGenerator.generateToken(issuer, ttl, userName, serviceId, grantType);
        String token2 = JwTokenGenerator.generateToken(issuer, ttl, userName, serviceId, grantType);

        assertThat(token1.length() > 200);
        assertThat(token2.length() > 200);

        assertNotSame(token1, token2);
    }

}
