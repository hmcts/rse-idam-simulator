package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.UserService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.JUnitAssertionsShouldIncludeMessage"})
@WebMvcTest
public class IdamSimulatorControllersNegativeHappyPathTest {

    public static final String AUTHORIZATION = "authorization";
    public static final String VALIDE_CODE = "123456";

    @MockBean
    OpenIdConfigService openIdConfigService;

    @MockBean
    JsonWebKeyService jsonWebKeyService;

    @MockBean
    private SimulatorService simulatorService;

    @MockBean
    private UserService userService;

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Legacy endpoint that should accept only Basic Auth")
    @Test
    public void legacyEndpointOauth2Token() throws Exception {
        assertNotNull(simulatorService);
        assertNotNull(userService);
        mockMvc.perform(post("/oauth2/authorize")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header(AUTHORIZATION, "wrongOne")
                            .param("redirect_uri", "aRedirectUrl")
                            .param("client_id", "hmcts")
                            .param("response_type", "code"))
            .andExpect(status().isUnauthorized())
            .andReturn();
    }

    @DisplayName("Grant types must be only authorization_code")
    @Test
    public void oauth2tokenCredentialChecks() throws Exception {
        assertNotNull(userService);

        postOauthToken("Wrong", VALIDE_CODE, status().isBadRequest());
        postOauthToken("authorization_code", VALIDE_CODE, status().isOk());
        postOauthToken("client_credentials", VALIDE_CODE, status().isBadRequest());
    }

    @DisplayName("Grant type authorization_code required a code")
    @Test
    public void oauth2tokenCredentialAuthCodeTest() throws Exception {
        assertNotNull(userService);

        postOauthToken("authorization_code", "", status().isBadRequest());
        postOauthToken("client_credentials", "", status().isBadRequest());
        postOauthToken("authorization_code", VALIDE_CODE, status().isOk());
    }

    @DisplayName("An unknown user id should be Not Found rather than a server error")
    @Test
    public void unknownUserIdIsNotFound() throws Exception {
        // getByUserId returns null for an id that was never added; letting that null flow on
        // produced a 500, which looks like the simulator is broken rather than the user missing.
        when(userService.getByUserId(anyString())).thenReturn(null);

        mockMvc.perform(get("/api/v1/users/noSuchUserId").header(AUTHORIZATION, "Bearer aToken"))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    private void postOauthToken(String grantType, String code, ResultMatcher expectedStatus) throws Exception {
        mockMvc.perform(post("/oauth2/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("redirect_uri", "aRedirectUrl")
                            .param("client_id", "hmcts")
                            .param("client_secret", "aSecret")
                            .param("grant_type", grantType)
                            .param("code", code))
            .andExpect(expectedStatus)
            .andReturn();
    }

}
