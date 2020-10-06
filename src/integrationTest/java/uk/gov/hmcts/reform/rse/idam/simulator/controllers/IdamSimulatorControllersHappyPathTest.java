package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@WebMvcTest
public class IdamSimulatorControllersHappyPathTest {

    public static final String AUTHORIZATION = "authorization";
    public static final String TOKEN = "Bearer eyJraWQiOiIyMzQ1Njc4OSIsImFsZyI6IlJTMjU2In0."
        + "eyJzdWIiOiJSU0UtSWRhbS1TaW11bGF0b3IiLCJpc3MiOiJodHRwOlwvXC9mci1hbTo4MDgwXC9vcGVuYW1cL29hdXRoMlwvaG1jdHMiL"
        + "CJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJleHAiOjE2MDEzOTIzMTQsImlhdCI6MTYwMTM3NzkxNH0.U-XyxFHq5daqQbbrnZQjV2V"
        + "qY7WVN3JA94WnwXF8tSCZSGb_GyfS0wu5DEtq-FPKzDbajuI2do-H6ElRM0Ko7Ch6qFFxvfF5riVVRHO3q0SjmkroP-faz_NqE3-UNrLTm0"
        + "zglYndemBw8h1hYVeJY95BZexXtO8SZOTKlUYnbfGSSL86WwPlk7wc3jH4CVbBI0hpaUvtoAfsvcZqqROUbQvJrIdsUirlMM6EkSnitfkcea"
        + "_H5qXC8nPOoMvhjocZBsJYVno4i8R7ildtHHZXCM_rz7dnU8XA2Mtj0o0DdoCbmAfYOuVEK7iZ1UQIwgZ8UUPEGk_N8t2gSxjM-cEtzg";
    public static final String BEARER_TOKEN = "Bearer " + TOKEN;
    public static final String BASIC_FOO = "Basic foo";
    public static final String TEST_EMAIL_HMCTS_NET = "test-email@hmcts.net";
    public static final String JOHN = "John";
    public static final String SMITH = "Smith";
    public static final String ROLE_1 = "role1";
    public static final String ROLE_2 = "role2";
    public static final String ONE_USER_ID = "oneUserId";
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    @MockBean
    SimulatorService simulatorService;

    @MockBean
    LiveMemoryService liveMemoryService;

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Should generate a pin code")
    @Test
    public void returnPinToken() throws Exception {
        PinDetails pinDetails = new PinDetails();
        pinDetails.setPin("eEdhNnasWy7eNFAV");
        pinDetails.setUserId("oneUserId");
        when(simulatorService.createPinDetails(any())).thenReturn(pinDetails);

        mockMvc.perform(post("/pin")
                            .header(AUTHORIZATION, BEARER_TOKEN)
                            .content(
                                "{ \"firstName\": \"Jane\", \"lastName\": \"Doe\", \"roles\":[\"role1\",\"role2\"] }")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pin").value("eEdhNnasWy7eNFAV"))
            .andExpect(jsonPath("$.userId").value("oneUserId"))
            .andReturn();
    }

    @DisplayName("Should return a pin code")
    @Test
    public void returnPinCode() throws Exception {
        mockMvc.perform(get("/pin")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header("pin", "pinHeaderValue")
                            .param(CLIENT_ID, "aClientId")
                            .param(REDIRECT_URI, "aRedirectUri")
                            .param("state", "oneState"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("dummyValue"))
            .andReturn();
    }

    @DisplayName("Legacy endpoint that Should return an oauth 2 token")
    @Test
    public void legacyEndpointOauth2Token() throws Exception {
        when(liveMemoryService.getByEmail(anyString())).thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));
        when(simulatorService.generateOauth2Code(anyString())).thenReturn("abcdefgh123456789");

        mockMvc.perform(post("/oauth2/authorize")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header(AUTHORIZATION, BASIC_FOO)
                            .param(REDIRECT_URI, "aRedirectUrl")
                            .param(CLIENT_ID, "aClientIdValue")
                            .param("response_type", "code"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("abcdefgh123456789"))
            .andReturn();

        verify(liveMemoryService, times(0))
            .putSimObject(Mockito.anyString(), Mockito.any(SimObject.class));

        verify(simulatorService, times(1))
            .generateOauth2Code(Mockito.anyString());
    }

    @DisplayName("Should return an user details")
    @Test
    public void returnOneUserDetails() throws Exception {
        when(liveMemoryService.getByBearerToken(anyString()))
            .thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));

        mockMvc.perform(get("/details").header(AUTHORIZATION, BEARER_TOKEN))
            .andExpect(jsonPath("$.id").value(ONE_USER_ID))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL_HMCTS_NET))
            .andExpect(jsonPath("$.forename").value(JOHN))
            .andExpect(jsonPath("$.surname").value(SMITH))
            .andExpect(jsonPath("$.roles[0]").value(ROLE_1))
            .andExpect(jsonPath("$.roles[1]").value(ROLE_2))
            .andReturn();
    }

    @DisplayName("Should return an oauth 2 token")
    @Test
    public void returnOauth2Token() throws Exception {
        when(simulatorService.generateAuthTokenFromCode(anyString())).thenReturn(TOKEN);

        mockMvc.perform(post("/oauth2/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("grant_type", "client_credential")
                            .param(REDIRECT_URI, "aRedirectUrl")
                            .param(CLIENT_ID, "aClientIdValue")
                            .param("code", "1234")
                            .param("client_secret", "oneSecret"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        verify(simulatorService, times(1))
            .generateAuthTokenFromCode(Mockito.anyString());
    }

    @DisplayName("Should return an open id token")
    @Test
    public void returnOpenIdToken() throws Exception {
        when(simulatorService.generateAToken()).thenReturn(TOKEN);
        when(liveMemoryService.getByEmail(anyString())).thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));

        mockMvc.perform(post("/o/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param(CLIENT_ID, "oneClientId")
                            .param("client_secret", "oneClientSecret")
                            .param("grant_type", "grantable")
                            .param(REDIRECT_URI, "aRedirectUrl")
                            .param("username", "aUserName")
                            .param("password", "somePassword")
                            .param("scope", "aScope"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andExpect(jsonPath("$.expires_in").isString())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.refresh_token").isString())
            .andExpect(jsonPath("$.scope").value("openid profile roles"))
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        verify(simulatorService, times(3))
            .generateAToken();
        verify(simulatorService, times(1))
            .updateTokenInUser("aUserName", TOKEN);
    }

    @DisplayName("Should return expected user info")
    @Test
    public void returnUserInfo() throws Exception {
        when(liveMemoryService.getByBearerToken(anyString()))
            .thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));
        mockMvc.perform(get("/o/userinfo").header(AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uid").value(ONE_USER_ID))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL_HMCTS_NET))
            .andExpect(jsonPath("$.given_name").value(JOHN))
            .andExpect(jsonPath("$.family_name").value(SMITH))
            .andExpect(jsonPath("$.sub").value("oneSub"))
            .andExpect(jsonPath("$.roles[0]").value(ROLE_1))
            .andExpect(jsonPath("$.roles[1]").value(ROLE_2))
            .andReturn();
    }

    @DisplayName("Should return expected user details")
    @Test
    public void returnUserDetails() throws Exception {
        when(liveMemoryService.getByBearerToken(anyString()))
            .thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));
        when(liveMemoryService.getByUserId(anyString())).thenReturn(SimulatorDataFactory.createSimObject());

        mockMvc.perform(get("/api/v1/users/" + 123).header(AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(ONE_USER_ID))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL_HMCTS_NET))
            .andExpect(jsonPath("$.forename").value(JOHN))
            .andExpect(jsonPath("$.surname").value(SMITH))
            .andExpect(jsonPath("$.roles[0]").value(ROLE_1))
            .andExpect(jsonPath("$.roles[1]").value(ROLE_2))
            .andReturn();
    }

    @DisplayName("Should return expected user details using a query")
    @Test
    public void searchUserDetails() throws Exception {
        when(liveMemoryService.getByBearerToken(anyString()))
            .thenReturn(Optional.of(SimulatorDataFactory.createSimObject()));
        when(liveMemoryService.getByUserId(anyString())).thenReturn(SimulatorDataFactory.createSimObject());

        mockMvc.perform(get("/api/v1/users")
                            .param("query", "anElasticSearchQuery")
                            .header(AUTHORIZATION, BEARER_TOKEN))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("oneUUIDValue"))
            .andExpect(jsonPath("$[0].email").value(TEST_EMAIL_HMCTS_NET))
            .andExpect(jsonPath("$[0].forename").value(JOHN))
            .andExpect(jsonPath("$[0].surname").value(SMITH))
            .andExpect(jsonPath("$[0].roles[0]").value(ROLE_1))
            .andExpect(jsonPath("$[0].roles[1]").value(ROLE_2))

            .andReturn();
    }

}
