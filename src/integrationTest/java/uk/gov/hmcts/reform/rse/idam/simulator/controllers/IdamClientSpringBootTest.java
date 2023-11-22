package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.AuthenticateUserResponse;
import uk.gov.hmcts.reform.idam.client.models.ExchangeCodeRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenExchangeResponse;
import uk.gov.hmcts.reform.idam.client.models.TokenResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamTestingUser;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.RoleDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.PersistentStorageService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.UserService;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * In short we have 3 ways to have a Bearer Token, this is indirectly tested in this integration test.
 * - Post o/token (username, password) --> Token
 * - Post Oauth2/authorize (Authorization Header Basic) -->  code
 * Post Oauth2/token (code) --> Token
 * - Post pin (Authorization Header Basic) --> pin
 * GET pin (pin) --> 301 with Header Location in form url?code=myvalue
 * Post Oauth2/token (code) --> Token
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.JUnitAssertionsShouldIncludeMessage",
    "PMD.JUnitTestsShouldIncludeAssert", "PMD.LawOfDemeter", "PMD.ExcessiveImports"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, IdamSimulatorController.class,
    LiveMemoryService.class, PersistentStorageService.class, SimulatorService.class, JsonWebKeyService.class,
    JwTokenGeneratorService.class, OpenIdConfigService.class},
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.yaml")
@EnableAutoConfiguration
public class IdamClientSpringBootTest {

    public static final String MYEMAIL_HMCTSTEST_NET = "myemail@hmctstest.net";
    public static final String THE_KID = "The Kid";
    public static final String BILLY = "Billy";
    public static final int BEARER_SIZE = 400;
    public static final int PIN_SIZE = 8;
    public static final int CODE_SIZE = 27;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    int localServerPort;

    @Autowired
    SimulatorService simulatorService;

    @Autowired
    UserService userService;

    @Autowired
    IdamClient idamClient;

    String accessToken;

    @BeforeEach
    public void setUp() {
        String userName = MYEMAIL_HMCTSTEST_NET;
        addUserToSimulator(BILLY, THE_KID, userName);
        accessToken = fetchAccessToken(MYEMAIL_HMCTSTEST_NET);
        assertNotNull(userService);
    }

    @Test
    public void oauth2authenticateUserTest() throws Exception {

        String deprecatedToken = idamClient.authenticateUser(MYEMAIL_HMCTSTEST_NET, "somePassword");

        assertTrue(deprecatedToken.startsWith("Bearer "));
        assertTrue(deprecatedToken.length() >= BEARER_SIZE);
    }

    @Test
    public void openIdGetAccessTokenTest() {
        assertTrue(accessToken.startsWith("Bearer "));
        assertTrue(accessToken.length() >= BEARER_SIZE);
    }

    @Test
    public void canSearchUserTest() {
        List<UserDetails> returnedUser = idamClient.searchUsers(accessToken, "return Smith");

        assertEquals(1, returnedUser.size());
        assertEquals(Optional.of("Smith"), returnedUser.get(0).getSurname());
    }

    @Test
    public void fetchUserInfoTest() {
        UserInfo userInfo = idamClient.getUserInfo(accessToken);

        assertNotNull(userInfo);
        assertEquals(THE_KID, userInfo.getFamilyName());
    }

    @Test
    public void fetchUserDetailsTest() {
        UserDetails userDetails = idamClient.getUserDetails(accessToken);

        assertEquals(Optional.of(THE_KID), userDetails.getSurname());
    }

    @Test
    public void fetchUserByUserId() {
        IdamUserInfo idamUserInfo = addUserToSimulator(
            "ForeName1",
            "SureName1",
            "AnotherEmail@hmcts.net"
        );

        UserDetails userDetails = idamClient.getUserByUserId(accessToken, idamUserInfo.getUid());

        assertEquals(Optional.of("SureName1"), userDetails.getSurname());
        assertEquals("ForeName1", userDetails.getForename());
        assertEquals("AnotherEmail@hmcts.net", userDetails.getEmail());
        assertNotNull(userDetails.getId(), idamUserInfo.getUid());
    }

    @Test
    public void fetchAccessTokenByUserPassword() {
        TokenResponse tokenResponse = idamClient.getAccessTokenResponse(
            MYEMAIL_HMCTSTEST_NET,
            "somePassword"
        );
        assertEquals(tokenResponse.expiresIn, "28800000");
        assertEquals(tokenResponse.scope, "openid profile roles");
        assertEquals(tokenResponse.tokenType, "Bearer");
        assertNotNull(tokenResponse.accessToken);
        assertTrue(tokenResponse.accessToken.length() >= BEARER_SIZE);

        //Token should be keep in cache until expiration
        TokenResponse tokenResponse2 = idamClient.getAccessTokenResponse(
            MYEMAIL_HMCTSTEST_NET,
            "somePassword"
        );
        assertEquals(tokenResponse.accessToken, tokenResponse2.accessToken, "Tokens should be the same");
    }

    @Test
    public void generatePinTest() {
        GeneratePinRequest pinRequest = GeneratePinRequest.builder()
            .firstName(BILLY).lastName(THE_KID)
            .build();
        GeneratePinResponse generatePinResponse = idamClient.generatePin(pinRequest, accessToken);
        // Not sure it should be an access token, it's probably more a Basic One.

        assertNotNull(generatePinResponse.getPin());
        assertSame(generatePinResponse.getPin().length(), PIN_SIZE);
        assertNotNull(generatePinResponse.getUserId());
    }

    @Test
    public void authPinUser() throws UnsupportedEncodingException {

        GeneratePinRequest pinRequest = GeneratePinRequest.builder()
            .firstName(BILLY).lastName(THE_KID)
            .build();
        GeneratePinResponse generatePinResponse = idamClient.generatePin(pinRequest, accessToken);

        AuthenticateUserResponse authUserResponse = idamClient.authenticatePinUser(
            generatePinResponse.getPin(),
            "someState"
        );

        assertNotNull(authUserResponse.getCode());
        assertSame(authUserResponse.getCode().length(), CODE_SIZE);

    }

    @Test
    public void exchangeCode() throws UnsupportedEncodingException {

        GeneratePinRequest pinRequest = GeneratePinRequest.builder()
            .firstName(BILLY).lastName(THE_KID)
            .build();
        GeneratePinResponse generatePinResponse = idamClient.generatePin(pinRequest, accessToken);

        AuthenticateUserResponse authenticateUserResponse = idamClient.authenticatePinUser(
            generatePinResponse.getPin(),
            "someState"
        );

        ExchangeCodeRequest exchangeRequest = new ExchangeCodeRequest(
            authenticateUserResponse.getCode(),
            "authorization_code",
            "https://dummyStuff:3000/receiver",
            "hmcts",
            "aClientSecret"
        );

        TokenExchangeResponse tokenExchangeResponse = idamClient.exchangeCode(exchangeRequest);

        assertNotNull(tokenExchangeResponse.getAccessToken());
        assertTrue(tokenExchangeResponse.getAccessToken().length() >= BEARER_SIZE);

    }

    @Test
    public void accessTokenIsNotValidPrefixTest() {
        expect401Error(() -> {
            idamClient.getUserDetails("something wrong");
        });

        expect401Error(() -> {
            idamClient.searchUsers("wrong token", "a query");
        });

        expect401Error(() -> {
            idamClient.getUserByUserId("Wrong token", "One id");
        });

        expect401Error(() -> {
            idamClient.getUserInfo("something wrong");
        });
    }

    @Test
    public void unauthorizedForUnknownUser() {
        expect401Error(() -> {
            idamClient.getAccessToken("unknowUser@gmail.com", "onePassword");
        });
    }

    private String fetchAccessToken(String email) {
        return idamClient.getAccessToken(email, "onePassword");
    }

    private void expect401Error(Executable apiCall) {
        FeignException exception = assertThrows(FeignException.class, apiCall);
        assertEquals(exception.status(), 401);
    }

    private IdamUserInfo addUserToSimulator(String forename, String surename, String email) {
        final String uri = "http://localhost:" + localServerPort + "/testing-support/accounts";
        RestTemplate restTemplate = new RestTemplate();

        IdamTestingUser idamTestingUser = new IdamTestingUser();
        idamTestingUser.setEmail(email);
        idamTestingUser.setForename(forename);
        idamTestingUser.setSurname(surename);
        idamTestingUser.setRoles(Stream.of("role1", "role2").map(RoleDetails::build).collect(Collectors.toList()));
        idamTestingUser.setPassword("OnePassword");

        String postForObject = restTemplate.postForObject(uri, idamTestingUser, String.class);

        String userUuid = extractUserUid(postForObject);

        IdamUserInfo idamUserInfo = new IdamUserInfo();
        idamUserInfo.setRoles(idamTestingUser.getRoles().stream().map(RoleDetails::getCode)
                                  .collect(Collectors.toList()));
        idamUserInfo.setEmail(idamTestingUser.getEmail());
        idamUserInfo.setFamilyName(idamTestingUser.getSurname());
        idamUserInfo.setGivenName(idamTestingUser.getForename());
        idamUserInfo.setUid(userUuid);
        idamUserInfo.setSub(idamTestingUser.getEmail());
        return idamUserInfo;
    }

    private String extractUserUid(String postForObject) {
        try {
            return objectMapper.readTree(postForObject).path("uuid").asText();
        } catch (JsonProcessingException jse) {
            throw new JsonTestException("impossible to extract uuid", jse);
        }
    }

}
