package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.AuthenticateUserResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.GeneratePinRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenExchangeResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGenerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
@RestController
public class IdamSimulatorController {

    private static final Logger LOG = LoggerFactory.getLogger(IdamSimulatorController.class);
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    @Autowired
    private LiveMemoryService liveMemoryService;

    @Value("${simulator.jwt.issuer}")
    private String issuer;

    @Value("${simulator.jwt.expiration}")
    private long expiration;

    /*
    This method is no longer acceptable as idam now uses OpenID Connect and /oauth2/authorize endpoint is deprecated.
    */
    @Deprecated
    @PostMapping(value = "/oauth2/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public AuthenticateUserResponse authoriseUser(@RequestHeader(AUTHORIZATION) String authorization,
                                                  @RequestParam(CLIENT_ID) final String clientId,
                                                  @RequestParam(REDIRECT_URI) final String redirectUri,
                                                  @RequestParam("response_type") final String responseType
    ) {
        LOG.warn("oauth2/authorize endpoint is deprecated!");
        LOG.info("Request oauth2 authorise for clientId {}", clientId);
        return SimulatorDataFactory.OAUTH2_USER_PASSWORD_RESPONSE;
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenExchangeResponse oauth2Token(@RequestParam(CLIENT_ID) final String clientId,
                                             @RequestParam(REDIRECT_URI) final String redirectUri,
                                             @RequestParam("client_secret") final String clientSecret,
                                             @RequestParam("grant_type") final String grantType,
                                             @RequestParam("code") final String code) {
        LOG.info("Request oauth2 token for code {} and clientId {}", code, clientId);
        TokenExchangeResponse tokenExchangeResponse = createTokenExchangeResponse();
        LOG.info("Oauth2 Token Generated {}", tokenExchangeResponse.accessToken);
        liveMemoryService.putSimObject(
            tokenExchangeResponse.accessToken, SimObject.builder().clientId(clientId).build());
        return tokenExchangeResponse;
    }

    @PostMapping(value = "/o/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse getOpenIdToken(@RequestParam(CLIENT_ID) final String clientId,
                                        @RequestParam(REDIRECT_URI) final String redirectUri,
                                        @RequestParam("client_secret") final String clientSecret,
                                        @RequestParam("grant_type") final String grantType,
                                        @RequestParam("username") final String username,
                                        @RequestParam("password") final String password,
                                        @RequestParam("scope") final String scope) {
        LOG.info("Request OpenId Token for clientId {} Username {} and scope {}", clientId, username, scope);
        TokenResponse token = createToken();
        LOG.info("Access Open Id Token Generated {}", token.accessToken);
        liveMemoryService.putSimObject(
            token.accessToken,
            SimObject.builder()
                .clientId(clientId)
                .username(username)
                .build()
        );
        return token;
    }

    @PostMapping("/pin")
    public PinDetails postPin(@RequestBody GeneratePinRequest request,
                              @RequestHeader(AUTHORIZATION) String authorization) {
        LOG.info("Post Request Pin for {}", request.getFirstName());
        return createPinDetails();
    }

    @GetMapping(value = "/pin", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> getPin(@RequestHeader("pin") final String pin,
                                         @RequestParam(CLIENT_ID) final String clientId,
                                         @RequestParam(REDIRECT_URI) final String redirectUri,
                                         @RequestParam("state") final String state) {
        LOG.info("Get Pin for pin {}", pin);
        Map<String, Object> body = new ConcurrentHashMap<>();
        body.put("code", "dummyValue");
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/details")
    public IdamUserDetails getDetails(@RequestHeader(AUTHORIZATION) String authorization) {
        return createUserDetails("NotSureProbablyExtractFromHeader");
    }

    @GetMapping("/o/userinfo")
    public IdamUserInfo getUserInfo(@RequestHeader(AUTHORIZATION) String authorization) {
        return createUserInfo("anUuidFromToken");
    }

    @GetMapping("/api/v1/users/{userId}")
    public IdamUserDetails getUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable("userId") String userId) {
        LOG.info("Request User Details {}", userId);
        return createUserDetails(userId);
    }

    @GetMapping("/api/v1/users")
    public List<IdamUserDetails> getSearchUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestParam("query") final String elasticSearchQuery) {
        LOG.info("Request Search user details with query {}", elasticSearchQuery);
        return createUserDetailsList();
    }

    private List<IdamUserDetails> createUserDetailsList() {
        return Collections.singletonList(createUserDetails("oneUUIDValue"));
    }

    private IdamUserDetails createUserDetails(String userId) {
        IdamUserDetails userDetails = new IdamUserDetails();
        userDetails.setId(userId);
        userDetails.setEmail("test-email@hmcts.net");
        userDetails.setForename("John");
        userDetails.setSurname("Smith");
        userDetails.setRoles(Arrays.asList("role1", "role2"));
        return userDetails;
    }

    private IdamUserInfo createUserInfo(String uuid) {
        IdamUserInfo idamUserInfo = new IdamUserInfo();
        idamUserInfo.setEmail("test-email@hmcts.net");
        idamUserInfo.setFamilyName("Smith");
        idamUserInfo.setGivenName("John");
        idamUserInfo.setName("Johnny");
        idamUserInfo.setRoles(Arrays.asList("role1", "role2"));
        idamUserInfo.setSub("sub99");
        idamUserInfo.setUid(uuid);
        return idamUserInfo;
    }

    private TokenResponse createToken() {
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        String refreshToken = JwTokenGenerator.generateToken(issuer, expiration);
        String idToken = JwTokenGenerator.generateToken(issuer, expiration);

        return new TokenResponse(token, String.valueOf(expiration), idToken, refreshToken,
                                 "openid profile roles search-user", "Bearer"
        );
    }

    private TokenExchangeResponse createTokenExchangeResponse() {
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        return new TokenExchangeResponse(token);
    }

    private PinDetails createPinDetails() {
        PinDetails pin = new PinDetails();
        pin.setPin("1234");
        pin.setUserId("NotSureProbablyExtractFromHeader");
        return pin;
    }

}
