package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.AuthenticateUserRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.AuthenticateUserResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.ExchangeCodeRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.GeneratePinRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.token.JwTokenGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SuppressWarnings("PMD.UseObjectForClearerAPI")
@RestController
public class IdamSimulatorController {

    private static final Logger LOG = LoggerFactory.getLogger(IdamSimulatorController.class);

    @Value("${simulator.jwt.issuer}")
    private String issuer;

    @Value("${simulator.jwt.expiration}")
    private long expiration;

    @PostMapping("/pin")
    public PinDetails postPin(@RequestBody GeneratePinRequest request,
                              @RequestHeader(AUTHORIZATION) String authorization) {
        LOG.info("Post Request Pin for {}", request.getFirstName());
        return createPinDetails();
    }

    @GetMapping(value = "/pin", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> getPin(@RequestHeader("pin") final String pin,
                                         @RequestParam("client_id") final String clientId,
                                         @RequestParam("redirect_uri") final String redirectUri,
                                         @RequestParam("state") final String state) {
        LOG.info("Get Pin for pin {}", pin);
        Map<String, Object> body = new ConcurrentHashMap<>();
        body.put("code", "dummyValue");
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    /*
    This method is no longer acceptable as idam now uses OpenID Connect and /oauth2/authorize endpoint is deprecated.
    */
    @Deprecated
    @PostMapping(value = "/oauth2/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public AuthenticateUserResponse authoriseUser(@RequestHeader(AUTHORIZATION) String authorization,
                                                  AuthenticateUserRequest request) {
        LOG.warn("oauth2/authorize endpoint is deprecated!");
        LOG.info("Request oauth2 authorise for clientId {}", request.getClientId());
        return SimulatorDataFactory.OAUTH2_USER_PASSWORD_RESPONSE;
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> oauth2Token(ExchangeCodeRequest request) {
        LOG.info("Request oauth2 token for code {} and grantType {}", request.getCode(), request.getGrantType());
        ResponseEntity<Object> tokenExchangeResponse = createTokenExchangeResponse();
        LOG.info("Oauth2 Token Generated {}", tokenExchangeResponse.getBody());
        return tokenExchangeResponse;
    }

    @GetMapping("/details")
    public IdamUserDetails getDetails(@RequestHeader(AUTHORIZATION) String authorization) {
        return createUserDetails("NotSureProbablyExtractFromHeader");
    }

    @PostMapping(value = "/o/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse getOpenIdToken(TokenRequest request) {
        LOG.info("Request OpenId Token for {}", request.getUsername());
        TokenResponse token = createToken();
        LOG.info("Access Open Id Token Generated {}", token.accessToken);
        return token;
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
        return Arrays.asList(createUserDetails("oneUUIDValue"));
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

    private ResponseEntity<Object> createTokenExchangeResponse() {
        Map<String, Object> body = new ConcurrentHashMap<>();
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        body.put("access_token", token);
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    private PinDetails createPinDetails() {
        PinDetails pin = new PinDetails();
        pin.setPin("1234");
        pin.setUserId("NotSureProbablyExtractFromHeader");
        return pin;
    }

}
