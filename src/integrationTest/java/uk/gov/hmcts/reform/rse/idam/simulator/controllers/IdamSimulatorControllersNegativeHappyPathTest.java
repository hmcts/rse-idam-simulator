package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.JUnitAssertionsShouldIncludeMessage"})
@WebMvcTest
public class IdamSimulatorControllersNegativeHappyPathTest {

    public static final String AUTHORIZATION = "authorization";

    @MockBean
    private LiveMemoryService liveMemoryService;

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Legacy endpoint that should accept only Basic Auth")
    @Test
    public void legacyEndpointOauth2Token() throws Exception {
        assertNotNull(liveMemoryService);
        mockMvc.perform(post("/oauth2/authorize")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header(AUTHORIZATION, "wrongOne")
                            .param("redirect_uri", "aRedirectUrl")
                            .param("client_id", "aClientIdValue")
                            .param("response_type", "code"))
            .andExpect(status().isUnauthorized())
            .andReturn();
    }

}
