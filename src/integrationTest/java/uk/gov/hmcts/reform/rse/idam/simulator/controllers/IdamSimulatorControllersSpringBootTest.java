package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorDataFactory;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.idam.client.IdamClient.GRANT_TYPE;

@SuppressWarnings({"PMD.JUnitTestsShouldIncludeAssert", "PMD.JUnitAssertionsShouldIncludeMessage"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, IdamSimulatorController.class,
    LiveMemoryService.class, SimulatorService.class, JsonWebKeyService.class,
    JwTokenGeneratorService.class, OpenIdConfigService.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.yaml")
@DirtiesContext
@EnableAutoConfiguration
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class IdamSimulatorControllersSpringBootTest {
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String CLIENT_ID_HMCTS = "hmcts";
    private static final String CLIENT_SECRET = "client_secret";
    private static final String GRANT_TYPE_NAME = "grant_type";
    private static final String IDAM_SCOPE = "scope";
    private static final String AUTH_TYPE_CODE = "code";
    private static final String VALID_CODE = "itsAnOlderCodeSirButItChecksOut";

    @SpyBean
    SimulatorService simulatorService;

    @SpyBean
    LiveMemoryService liveMemoryService;

    @Autowired
    private transient MockMvc mockMvc;

    @Before
    public void setup() {
        SimObject simObject = SimulatorDataFactory.createSimObject();
        simObject.setMostRecentCode(VALID_CODE);
        liveMemoryService.putSimObject(UUID.nameUUIDFromBytes(simObject.getEmail().getBytes()).toString(), simObject);
    }

    @DisplayName("Should return an open id token from code")
    @Test
    public void returnOpenIdTokenFromCode() throws Exception {

        mockMvc.perform(post("/o/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param(CLIENT_ID, CLIENT_ID_HMCTS)
                            .param(CLIENT_SECRET, "oneClientSecret")
                            .param(GRANT_TYPE_NAME, GRANT_TYPE)
                            .param(REDIRECT_URI, "aRedirectUrl")
                            .param(AUTH_TYPE_CODE, "itsAnOlderCodeSirButItChecksOut")
                            .param(IDAM_SCOPE, "openid profile roles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andExpect(jsonPath("$.expires_in").isString())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.refresh_token").isString())
            .andExpect(jsonPath("$.scope").value("openid profile roles"))
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        verify(liveMemoryService, times(3)).getByCode(VALID_CODE);
        verify(simulatorService, times(3))
            .generateAuthTokenFromCode(anyString(), anyString(), anyString());
    }

    @DisplayName("Code is invalid so deny access")
    @Test
    public void incorrectCode() throws Exception {

        mockMvc.perform(post("/o/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param(CLIENT_ID, CLIENT_ID_HMCTS)
                            .param(CLIENT_SECRET, "oneClientSecret")
                            .param(GRANT_TYPE_NAME, GRANT_TYPE)
                            .param(REDIRECT_URI, "aRedirectUrl")
                            .param(AUTH_TYPE_CODE, "opjkasdfi9opjasd")
                            .param(IDAM_SCOPE, "openid profile roles"))
            .andExpect(status().isUnauthorized())
            .andReturn();

        verify(simulatorService, never()).generateAToken(anyString(), anyString(), anyString());
    }
}
