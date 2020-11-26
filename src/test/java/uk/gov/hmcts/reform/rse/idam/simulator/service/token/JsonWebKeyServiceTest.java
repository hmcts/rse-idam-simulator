package uk.gov.hmcts.reform.rse.idam.simulator.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
@RunWith(MockitoJUnitRunner.class)
public class JsonWebKeyServiceTest {

    @Test
    public void generatorShouldReturnADifferentKeyEachTime() throws JOSEException {

        JsonWebKeyService jsonWebKeyService = new JsonWebKeyService();

        RSAKey rsaKey1 = jsonWebKeyService.generateNewRsaKey();
        RSAKey rsaKey2 = jsonWebKeyService.generateNewRsaKey();

        assertNotNull(rsaKey1.toPublicKey());
        assertNotNull(rsaKey2.toPublicKey());

        assertThat(rsaKey1.getModulus().toJSONString().length() > 100);
        assertThat(rsaKey2.getModulus().toJSONString().length() > 200);

        assertNotSame(rsaKey1.getModulus().toJSONString(), rsaKey2.getModulus().toJSONString());
    }

}
