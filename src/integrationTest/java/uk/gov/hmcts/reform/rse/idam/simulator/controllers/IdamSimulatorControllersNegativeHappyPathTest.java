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
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.JUnitAssertionsShouldIncludeMessage"})
@WebMvcTest
public class IdamSimulatorControllersNegativeHappyPathTest {

    public static final String AUTHORIZATION = "authorization";
    public static final String VALIDE_CODE = "123456";

    @MockBean
    JsonWebKeyService jsonWebKeyService;

    @MockBean
    private SimulatorService simulatorService;

    @MockBean
    private LiveMemoryService liveMemoryService;

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Legacy endpoint that should accept only Basic Auth")
    @Test
    public void legacyEndpointOauth2Token() throws Exception {
        assertNotNull(simulatorService);
        assertNotNull(liveMemoryService);
        mockMvc.perform(post("/oauth2/authorize")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header(AUTHORIZATION, "wrongOne")
                            .param("redirect_uri", "aRedirectUrl")
                            .param("client_id", "hmcts")
                            .param("response_type", "code"))
            .andExpect(status().isUnauthorized())
            .andReturn();
    }

    @DisplayName("Grant types must be only authorization_code or client_credentials")
    @Test
    public void oauth2tokenCredentialChecks() throws Exception {
        assertNotNull(liveMemoryService);

        postOauthToken("Wrong", VALIDE_CODE, status().isBadRequest());
        postOauthToken("authorization_code", VALIDE_CODE, status().isOk());
        postOauthToken("client_credentials", VALIDE_CODE, status().isOk());
    }

    @DisplayName("Grant type authorization_code required a code")
    @Test
    public void oauth2tokenCredentialAuthCodeTest() throws Exception {
        assertNotNull(liveMemoryService);

        postOauthToken("authorization_code", "", status().isBadRequest());
        postOauthToken("client_credentials", "", status().isOk());
        postOauthToken("authorization_code", VALIDE_CODE, status().isOk());
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
