package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import static org.junit.Assert.assertTrue;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, IdamSimulatorController.class}, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.yml")
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class IdamClientSpringBootTest {

    @Autowired
    IdamClient idamClient;

    @Test
    public void oauth2authenticateUserTest() {
        String deprecatedToken = idamClient.authenticateUser("usernameToto", "passwordToto");

        assertTrue("Bearer token not correct", deprecatedToken.startsWith("Bearer "));
        assertTrue("Bearer token is too short", deprecatedToken.length() > 575);
    }
}
