package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
@WebMvcTest
public class IdamSimulatorControllersHappyPathTest {

    public static final String AUTHORIZATION = "authorization";
    public static final String BEARER_FOO = "bearer foo";
    public static final String TEST_EMAIL_HMCTS_NET = "test-email@hmcts.net";
    public static final String JOHN = "John";
    public static final String SMITH = "Smith";
    public static final String ROLE_1 = "role1";
    public static final String ROLE_2 = "role2";

    @MockBean
    private LiveMemoryService liveMemoryService;

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Should generate a pin code")
    @Test
    public void generatePinCode() throws Exception {
        mockMvc.perform(post("/pin")
                            .header(AUTHORIZATION, BEARER_FOO)
                            .content(
                                "{ \"firstName\": \"Jane\", \"lastName\": \"Doe\", \"roles\":[\"role1\",\"role2\"] }")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pin").value("1234"))
            .andExpect(jsonPath("$.userId").value("NotSureProbablyExtractFromHeader"))
            .andReturn();
    }

    @DisplayName("Should return a pin code")
    @Test
    public void returnPinCode() throws Exception {
        mockMvc.perform(get("/pin")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header("pin", "pinHeaderValue")
                            .param("client_id", "aClientId")
                            .param("redirect_uri", "aRedirectUri")
                            .param("state", "oneState"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("dummyValue"))
            .andReturn();
    }

    @DisplayName("Should return an oauth 2 token")
    @Test
    public void returnPinToken() throws Exception {
        mockMvc.perform(post("/pin")
                            .content(
                                "{ \"firstName\": \"Jane\", \"lastName\": \"Doe\", \"roles\":[\"role1\",\"role2\"] }")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(AUTHORIZATION, BEARER_FOO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.pin").value("1234"))
            .andExpect(jsonPath("$.userId").value("NotSureProbablyExtractFromHeader"))
            .andReturn();
    }

    @DisplayName("Should return an user details")
    @Test
    public void returnOneUserDetails() throws Exception {
        mockMvc.perform(get("/details").header(AUTHORIZATION, BEARER_FOO))
            .andExpect(jsonPath("$.id").value("NotSureProbablyExtractFromHeader"))
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
        mockMvc.perform(post("/oauth2/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .header("my-number", "1234")
                            .param("grant_type", "client_credential")
                            .param("redirect_uri", "aRedirectUrl")
                            .param("client_id", "aClientIdValue")
                            .param("client_secret", "oneSecret"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        verify(liveMemoryService, times(1))
            .putSimObject(Mockito.anyString(), Mockito.any(SimObject.class));
    }

    @DisplayName("Should return an open id token")
    @Test
    public void returnOpenIdToken() throws Exception {
        mockMvc.perform(post("/o/token")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                            .param("access_token", "oneClientId")
                            .param("expires_in", "oneClientSecret")
                            .param("id_token", "client_credential")
                            .param("refresh_token", "aRedirectUrl")
                            .param("token_type", "Bearer"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andExpect(jsonPath("$.expires_in").isString())
            .andExpect(jsonPath("$.id_token").isString())
            .andExpect(jsonPath("$.refresh_token").isString())
            .andExpect(jsonPath("$.scope").value("openid profile roles search-user"))
            .andExpect(jsonPath("$.token_type").value("Bearer"))
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        verify(liveMemoryService, times(1))
            .putSimObject(Mockito.anyString(), Mockito.any(SimObject.class));
    }

    @DisplayName("Should return expected user info")
    @Test
    public void returnUserInfo() throws Exception {
        mockMvc.perform(get("/o/userinfo").header(AUTHORIZATION, BEARER_FOO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.uid").value("anUuidFromToken"))
            .andExpect(jsonPath("$.email").value(TEST_EMAIL_HMCTS_NET))
            .andExpect(jsonPath("$.given_name").value(JOHN))
            .andExpect(jsonPath("$.family_name").value(SMITH))
            .andExpect(jsonPath("$.sub").value("sub99"))
            .andExpect(jsonPath("$.roles[0]").value(ROLE_1))
            .andExpect(jsonPath("$.roles[1]").value(ROLE_2))
            .andReturn();
    }

    @DisplayName("Should return expected user details")
    @Test
    public void returnUserDetails() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + 123).header(AUTHORIZATION, BEARER_FOO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"))
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
        mockMvc.perform(get("/api/v1/users")
                            .param("query", "anElasticSearchQuery")
                            .header(AUTHORIZATION, BEARER_FOO))
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
