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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;

import static org.junit.Assert.assertTrue;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, IdamSimulatorController.class, LiveMemoryService.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.yml")
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class IdamClientSpringBootTest {

    @Autowired
    IdamClient idamClient;

    /*
    Idam Methods to test
            idamClient.
            .searchUsers()
            .getUserInfo()
            .getUserDetails()
            .getUserByUserId()
            .getAccessTokenResponse()
            .generatePin()
            .exchangeCode()
            .authenticatePinUser()
            .authenticateUser()// Done
            .getAccessToken()// Done
    * */


    @Test
    public void oauth2authenticateUserTest() {
        String deprecatedToken = idamClient.authenticateUser("someUserName", "somePassword");

        assertTrue("Bearer token not correct", deprecatedToken.startsWith("Bearer "));
        assertTrue("Bearer token is too short", deprecatedToken.length() > 575);
    }

    @Test
    public void openIdGetAccessToken() {
        String accessToken = fetchAccessToken();

        assertTrue("Bearer token not correct", accessToken.startsWith("Bearer "));
        assertTrue("Bearer token is too short", accessToken.length() > 575);
    }

    private String fetchAccessToken() {
        return idamClient.getAccessToken("oneUserName", "onePassword");
    }

    @Test
    public void accessTokenIsNotValid() {
        UserDetails userDetails = idamClient.getUserDetails("Bearer something wrong");
    }


}
