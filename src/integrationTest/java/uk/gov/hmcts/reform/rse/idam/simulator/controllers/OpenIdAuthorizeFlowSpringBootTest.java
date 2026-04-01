package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamTestingUser;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.RoleDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.PersistentStorageService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
    IdamSimulatorController.class,
    LoginController.class,
    LiveMemoryService.class,
    PersistentStorageService.class,
    SimulatorService.class,
    JsonWebKeyService.class,
    JwTokenGeneratorService.class,
    OpenIdConfigService.class
    }, properties = "spring.main.lazy-initialization=true")
@AutoConfigureMockMvc
@PropertySource("classpath:application.yaml")
@EnableAutoConfiguration
class OpenIdAuthorizeFlowSpringBootTest {

    private static final String CLIENT_ID = "hmcts";
    private static final String REDIRECT_URI = "https://localhost/receiver";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void authorizeWithLoginHintIssuesRedeemableCode() throws Exception {
        String email = uniqueEmail();
        addUser(email, "Billy", "Kid");
        MvcResult authorizeResult = mockMvc.perform(post("/o/authorize")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("client_id", CLIENT_ID)
                .param("redirect_uri", REDIRECT_URI)
                .param("state", "some-state")
                .param("response_type", "code")
                .param("login_hint", email))
            .andExpect(status().isFound())
            .andReturn();

        String redirectLocation = authorizeResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(redirectLocation);
        assertTrue(redirectLocation.startsWith(REDIRECT_URI));

        String discoveryIssuer = fetchDiscoveryIssuer();
        var redirectParams = UriComponentsBuilder.fromUriString(redirectLocation).build().getQueryParams();
        assertEquals("some-state", redirectParams.getFirst("state"));
        assertEquals(CLIENT_ID, redirectParams.getFirst("client_id"));
        assertEquals(discoveryIssuer, redirectParams.getFirst("iss"));
        String code = redirectParams.getFirst("code");
        assertNotNull(code);

        MvcResult tokenResult = mockMvc.perform(post("/o/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + HttpHeaders.encodeBasicAuth(CLIENT_ID, "a-secret", StandardCharsets.UTF_8)
                )
                .param("grant_type", "authorization_code")
                .param("redirect_uri", REDIRECT_URI)
                .param("code", code))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").isString())
            .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String accessToken = tokenJson.path("access_token").asText();
        String idToken = tokenJson.path("id_token").asText();
        assertNotNull(accessToken);
        assertTrue(!accessToken.isBlank());
        assertNotNull(idToken);
        assertEquals(discoveryIssuer, SignedJWT.parse(idToken).getJWTClaimsSet().getIssuer());

        mockMvc.perform(get("/o/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(email))
            .andExpect(jsonPath("$.given_name").value("Billy"))
            .andExpect(jsonPath("$.family_name").value("Kid"));
    }

    @Test
    void browserAuthorizeFlowPreservesNonceInIdToken() throws Exception {
        String email = uniqueEmail();
        String nonce = "nonce-12345";
        addUser(email, "Ada", "Lovelace");

        MvcResult authorizeResult = mockMvc.perform(get("/o/authorize")
                .param("client_id", CLIENT_ID)
                .param("redirect_uri", REDIRECT_URI)
                .param("state", "browser-state")
                .param("response_type", "code")
                .param("nonce", nonce))
            .andExpect(status().isFound())
            .andReturn();

        String loginLocation = authorizeResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(loginLocation);
        assertTrue(loginLocation.startsWith("/login"));
        var loginQueryParams = UriComponentsBuilder.fromUriString(loginLocation).build().getQueryParams();
        assertEquals(nonce, loginQueryParams.getFirst("nonce"));

        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", email)
                .param("password", "OnePassword")
                .param("redirect_uri", REDIRECT_URI)
                .param("client_id", CLIENT_ID)
                .param("state", "browser-state")
                .param("response_type", "code")
                .param("nonce", nonce)
                .param("ui_local", "en"))
            .andExpect(status().isFound())
            .andReturn();

        String redirectLocation = loginResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(redirectLocation);
        String code = UriComponentsBuilder.fromUriString(redirectLocation).build().getQueryParams().getFirst("code");
        assertNotNull(code);

        MvcResult tokenResult = mockMvc.perform(post("/o/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + HttpHeaders.encodeBasicAuth(CLIENT_ID, "a-secret", StandardCharsets.UTF_8)
                )
                .param("grant_type", "authorization_code")
                .param("redirect_uri", REDIRECT_URI)
                .param("code", code))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode tokenJson = objectMapper.readTree(tokenResult.getResponse().getContentAsString());
        String idToken = tokenJson.path("id_token").asText();
        assertEquals(nonce, SignedJWT.parse(idToken).getJWTClaimsSet().getStringClaim("nonce"));
    }

    @Test
    void authorizeWithoutLoginHintRedirectsToLogin() throws Exception {
        MvcResult authorizeResult = mockMvc.perform(get("/o/authorize")
                .param("client_id", CLIENT_ID)
                .param("redirect_uri", REDIRECT_URI)
                .param("state", "browser-state")
                .param("response_type", "code"))
            .andExpect(status().isFound())
            .andReturn();

        String loginLocation = authorizeResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(loginLocation);
        assertTrue(loginLocation.startsWith("/login"));

        var queryParams = UriComponentsBuilder.fromUriString(loginLocation).build().getQueryParams();
        assertEquals(REDIRECT_URI, queryParams.getFirst("redirect_uri"));
        assertEquals(CLIENT_ID, queryParams.getFirst("client_id"));
        assertEquals("browser-state", queryParams.getFirst("state"));
        assertEquals("code", queryParams.getFirst("response_type"));
    }

    @Test
    void openIdTokenReturnsUnauthorizedForUnknownCode() throws Exception {
        mockMvc.perform(post("/o/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Basic " + HttpHeaders.encodeBasicAuth(CLIENT_ID, "a-secret", StandardCharsets.UTF_8)
                )
                .param("grant_type", "authorization_code")
                .param("redirect_uri", REDIRECT_URI)
                .param("code", "unknown-auth-code-1234"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizationCodesAreSingleUseAndRefreshedPerFlow() throws Exception {
        String email = uniqueEmail();
        addUser(email, "Grace", "Hopper");

        String firstCode = authorizeForCode(email);
        String secondCode = authorizeForCode(email);
        assertTrue(!firstCode.equals(secondCode));

        exchangeCode(secondCode).andExpect(status().isOk());
        exchangeCode(secondCode).andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturnsValidSetCookieHeadersForIncomingCookies() throws Exception {
        String email = uniqueEmail();
        addUser(email, "Cookie", "Tester");

        MvcResult loginResult = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookie(new Cookie("original", "cookie-value"))
                .param("username", email)
                .param("password", "OnePassword")
                .param("redirect_uri", REDIRECT_URI)
                .param("client_id", CLIENT_ID)
                .param("state", "browser-state")
                .param("response_type", "code")
                .param("ui_local", "en"))
            .andExpect(status().isFound())
            .andReturn();

        List<String> setCookieHeaders = loginResult.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        assertTrue(setCookieHeaders.stream().anyMatch(header -> header.startsWith("original=cookie-value")));
        assertTrue(setCookieHeaders.stream().noneMatch(header -> header.contains("Cookie@")));
    }

    @Test
    void openIdTokenRejectsMalformedBasicAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/o/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, "Basic not-base64")
                .param("grant_type", "authorization_code")
                .param("redirect_uri", REDIRECT_URI)
                .param("code", "some-code-1234"))
            .andExpect(status().isBadRequest());
    }

    private void addUser(String email, String forename, String surname) throws Exception {
        IdamTestingUser idamTestingUser = new IdamTestingUser();
        idamTestingUser.setEmail(email);
        idamTestingUser.setForename(forename);
        idamTestingUser.setSurname(surname);
        idamTestingUser.setPassword("OnePassword");
        idamTestingUser.setRoles(List.of(RoleDetails.build("role1"), RoleDetails.build("role2")));

        mockMvc.perform(post("/testing-support/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(idamTestingUser)))
            .andExpect(status().isOk());
    }

    private String uniqueEmail() {
        return "oidc-" + UUID.randomUUID() + "@hmcts.net";
    }

    private String fetchDiscoveryIssuer() throws Exception {
        MvcResult discoveryResult = mockMvc.perform(get("/o/.well-known/openid-configuration"))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode discoveryJson = objectMapper.readTree(discoveryResult.getResponse().getContentAsString());
        return discoveryJson.path("issuer").asText();
    }

    private String authorizeForCode(String email) throws Exception {
        MvcResult authorizeResult = mockMvc.perform(post("/o/authorize")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("client_id", CLIENT_ID)
                .param("redirect_uri", REDIRECT_URI)
                .param("response_type", "code")
                .param("login_hint", email))
            .andExpect(status().isFound())
            .andReturn();

        String redirectLocation = authorizeResult.getResponse().getHeader(HttpHeaders.LOCATION);
        assertNotNull(redirectLocation);
        return UriComponentsBuilder.fromUriString(redirectLocation).build().getQueryParams().getFirst("code");
    }

    private ResultActions exchangeCode(String code) throws Exception {
        return mockMvc.perform(post("/o/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header(
                HttpHeaders.AUTHORIZATION,
                "Basic " + HttpHeaders.encodeBasicAuth(CLIENT_ID, "a-secret", StandardCharsets.UTF_8)
            )
            .param("grant_type", "authorization_code")
            .param("redirect_uri", REDIRECT_URI)
            .param("code", code));
    }
}
