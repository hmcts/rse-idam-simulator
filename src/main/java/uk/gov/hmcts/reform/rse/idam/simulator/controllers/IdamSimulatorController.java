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
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.AuthenticateUserResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.GeneratePinRequest;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamTestingUser;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserAddReponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.JsonWebKeySet;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.OpenIdConfig;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.RoleDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorDataFactory.getUserOne;

@SuppressWarnings({"PMD.ExcessiveImports",
    "PMD.UseObjectForClearerAPI",
    "PMD.TooManyMethods",
    "PMD.ExcessiveParameterList"})
@RestController
public class IdamSimulatorController {

    private static final Logger LOG = LoggerFactory.getLogger(IdamSimulatorController.class);
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    @Autowired
    private OpenIdConfigService openIdConfigService;

    @Autowired
    private JsonWebKeyService jsonWebKeyService;

    @Autowired
    private SimulatorService simulatorService;

    @Autowired
    private LiveMemoryService liveMemoryService;

    @Value("${simulator.jwt.expiration}")
    private long expiration;

    @Value("${simulator.jwt.issuer}")
    private String jwtIssuer;

    @Value("${simulator.openid.base-url}")
    private String idamBaseUrl;

    /*
    This tactical endpoint is replaced by OpenID /o/authorize.
    */
    @Deprecated
    @PostMapping(value = "/oauth2/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public AuthenticateUserResponse authoriseUser(@RequestHeader(AUTHORIZATION) String authorization,
                                                  @RequestParam(CLIENT_ID) final String clientId,
                                                  @RequestParam(value = REDIRECT_URI, required = false)
                                                      final String redirectUri,
                                                  @RequestParam(value = "response_type", required = false)
                                                      final String responseType
    ) {
        LOG.warn("oauth2/authorize endpoint is deprecated!");
        LOG.info("Request oauth2 authorise for clientId {}", clientId);
        checkUserAuthenticatedByAuthBasic(authorization);

        byte[] decoded = Base64.getDecoder().decode(authorization.replace("Basic ", ""));
        String username = new String(decoded).split(":")[0];

        String newCode = simulatorService.generateOauth2CodeFromUserName(username);
        return new AuthenticateUserResponse(newCode);
    }

    /*
    This tactical endpoint is replaced by OpenID /o/token.
     */
    @Deprecated
    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse oauth2Token(@RequestParam(value = CLIENT_ID, required = false) String clientId,
                                     @RequestParam(name = REDIRECT_URI, required = false) final String redirectUri,
                                     @RequestParam(value = "client_secret", required = false) String clientSecret,
                                     @RequestParam("grant_type") final String grantType,
                                     @RequestParam("code") final String code,
                                     @RequestHeader(value = AUTHORIZATION, required = false)
                                         final String authorization) {

        // Take client details from authorization if not in params.
        if (clientId == null || clientSecret == null) {
            var creds = new String(Base64.getDecoder().decode(authorization.replace("Basic ", "")))
                .split(":");
            clientId = creds[0];
        }
        LOG.warn("oauth2/token endpoint is deprecated!");
        LOG.info("Request oauth2 token for grantType {} code {} and clientId {}", grantType, code, clientId);

        checkGrantType(grantType);
        checkCode(code);

        String token = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        String refreshToken = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        String idToken = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        LOG.info("New oauth2 token Generated {}", token);

        simulatorService.updateTokenInUserFromCode(code, token);

        return new TokenResponse(token, String.valueOf(expiration), idToken, refreshToken,
                                 "openid profile roles", "Bearer"
        );
    }

    private void checkCode(String code) {
        if (code == null || code.length() < 4) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Idam Simulator: Code is missing or too short."
            );
        }
    }

    private void checkGrantType(String grantType) {
        //Notice than other credential type client_credentials is never used
        if (!(grantType.equalsIgnoreCase(GRANT_TYPE_AUTHORIZATION_CODE))) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Idam Simulator: Grand type not valid" + grantType + " , must be " + GRANT_TYPE_AUTHORIZATION_CODE
            );
        }
    }

    @PostMapping("/pin")
    public PinDetails postPin(@RequestBody GeneratePinRequest request) {
        LOG.info("Post Request Pin for {}", request.getFirstName());
        return simulatorService.createPinDetails(request.getFirstName(), request.getLastName());
    }

    @GetMapping(value = "/pin", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> getPin(@RequestHeader("pin") final String pin,
                                         @RequestParam(value = CLIENT_ID, required = false) final String clientId,
                                         @RequestParam(REDIRECT_URI) final String redirectUri,
                                         @RequestParam(value = "state", required = false) final String state) {
        LOG.info("Get Request Pin for pin {} to generate new code in Location Header", pin);
        HttpHeaders httpHeaders = new HttpHeaders();
        String generatedPinCode = simulatorService.generateOauth2CodeFromPin(pin);
        httpHeaders.add("Location", redirectUri + "?code=" + generatedPinCode);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    /*
    This tactical endpoint is replaced by OpenID /o/userinfo.
     */
    @Deprecated
    @GetMapping("/details")
    public IdamUserDetails getDetails(@RequestHeader(AUTHORIZATION) String authorization) {
        LOG.warn("/details endpoint is deprecated!");
        LOG.info("Request details with Authorization {}", authorization);
        simulatorService.checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByJwToken(authorization).get();
        return toUserDetails(simObject);
    }

    @GetMapping("/api/v1/users/{userId}")
    public IdamUserDetails getUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable("userId") String userId) {
        LOG.info("Request User Details {}", userId);
        simulatorService.checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByUserId(userId);
        return toUserDetails(simObject);
    }

    @GetMapping("/api/v1/users")
    public List<IdamUserDetails> getSearchUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestParam("query") final String elasticSearchQuery) {
        LOG.info("Request Search user details with query {}", elasticSearchQuery);
        simulatorService.checkUserHasBeenAuthenticateByBearerToken(authorization);
        return createUserDetailsList();
    }

    @PostMapping(value = "/o/token", params = "username", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse getOpenIdToken(@RequestParam(CLIENT_ID) final String clientId,
                                        @RequestParam(name = REDIRECT_URI, required = false) final String redirectUri,
                                        @RequestParam(value = "client_secret", required = false)
                                            final String clientSecret,
                                        @RequestParam("grant_type") final String grantType,
                                        @RequestParam("username") final String username,
                                        @RequestParam(value = "password", required = false)
                                            final String password,
                                        @RequestParam("scope") final String scope) {
        LOG.info(
            "Request OpenId Token for clientId {} Username {} scope {}",
            clientId,
            username,
            scope
        );
        String token = simulatorService.generateACachedToken(username, clientId, grantType);
        String refreshToken = simulatorService.generateAToken(username, clientId, grantType);
        String idToken = simulatorService.generateAToken(username, clientId, grantType);
        LOG.info("Access Open Id Token returned {}", token);
        simulatorService.updateTokenInUser(username, token);
        return new TokenResponse(token, String.valueOf(expiration), idToken, refreshToken,
                                 "openid profile roles", "Bearer"
        );
    }

    @PostMapping(value = "/o/token", params = "code", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse getOpenIdTokenFromCode(@RequestParam(CLIENT_ID) final String clientId,
                                                @RequestParam(name = REDIRECT_URI, required = false)
                                                final String redirectUri,
                                                @RequestParam(value = "client_secret", required = false)
                                                    final String clientSecret,
                                                @RequestParam("grant_type") final String grantType,

                                                final String password,
                                                @RequestParam(value = "scope", required = false) final String scope,
                                                @RequestParam(name = "code") final String code) {
        LOG.info(
            "Request OpenId Token for clientId {} scope {} and code {}",
            clientId,
            scope,
            code
        );

        checkGrantType(grantType);
        checkCode(code);

        String token = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        String refreshToken = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        String idToken = simulatorService.generateAuthTokenFromCode(code, clientId, grantType);
        LOG.info("Access Open Id Token generated from code {}", token);

        return new TokenResponse(token, String.valueOf(expiration), idToken, refreshToken,
                                 "openid profile roles", "Bearer"
        );
    }

    @GetMapping("/o/userinfo")
    public IdamUserInfo getUserInfo(@RequestHeader(AUTHORIZATION) String authorization) {
        LOG.info("Request o/userinfo with authorization {}", authorization);
        simulatorService.checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByJwToken(authorization).get();
        return toUserInfo(simObject);
    }

    @GetMapping("/o/jwks")
    public ResponseEntity<JsonWebKeySet> getJsonWebKeySet() {
        LOG.info("Request jwks config");
        JsonWebKeySet jsonWebKeySet = jsonWebKeyService.getJwkConfigSet();
        return ResponseEntity.ok(jsonWebKeySet);
    }

    @GetMapping("/o/.well-known/openid-configuration")
    public ResponseEntity<OpenIdConfig> getOpenIdConfig() {
        LOG.info("Request openIdConfig");
        OpenIdConfig openIdConfig = openIdConfigService
            .getOpenIdConfig(idamBaseUrl, jwtIssuer);
        return ResponseEntity.ok(openIdConfig);
    }

    @PostMapping(value = "/o/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> postToAuthorize(
        @RequestParam(value = "client_id", required = false) String clientId,
        @RequestParam(value = "redirect_uri", required = false) String redirectUri,
        @RequestParam(value = "state", required = false) String state,
        @RequestParam(value = "nonce", required = false) String nonce,
        @RequestParam(value = "response_type", required = false) String responseType,
        @RequestParam(value = "response_mode", required = false) String responseMode,
        @RequestParam(value = "display", required = false) String display,
        @RequestParam(value = "prompt", required = false) String prompt,
        @RequestParam(value = "max_age", required = false) String maxAge,
        @RequestParam(value = "acr_values", required = false) String acrValues,
        @RequestParam(value = "id_token_hint", required = false) String idTokenHint,
        @RequestParam(value = "login_hint", required = false) String loginHint) {

        LOG.info("Request OpenId Connect Code for clientId {}", clientId);

        HttpHeaders httpHeaders = new HttpHeaders();
        String generatedCode = simulatorService.getNewAuthCode();
        httpHeaders.add("Location", redirectUri + "?code=" + generatedCode);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    @GetMapping(value = "/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }

    private IdamUserInfo toUserInfo(SimObject simObject) {
        IdamUserInfo idamUserInfo = new IdamUserInfo();
        idamUserInfo.setFamilyName(simObject.getSurname());
        idamUserInfo.setGivenName(simObject.getForename());
        idamUserInfo.setUid(simObject.getId());
        idamUserInfo.setName(simObject.getForename() + " " + simObject.getSurname());
        idamUserInfo.setEmail(simObject.getEmail());
        // sub is an email in OpenId standard
        idamUserInfo.setSub(simObject.getEmail());
        idamUserInfo.setRoles(simObject.getRoles());
        return idamUserInfo;
    }

    private IdamUserDetails toUserDetails(SimObject simObject) {
        IdamUserDetails res = new IdamUserDetails();
        res.setEmail(simObject.getEmail());
        res.setForename(simObject.getForename());
        res.setId(simObject.getId());
        res.setSurname(simObject.getSurname());
        res.setRoles(simObject.getRoles());
        return res;
    }

    private List<IdamUserDetails> createUserDetailsList() {
        return Collections.singletonList(getUserOne("oneUUIDValue"));
    }

    private void checkUserAuthenticatedByAuthBasic(String authorization) {
        if (!authorization.startsWith("Basic")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Basic Auth Token required");
        }
    }

    /**
     * This endpoint is not part of Idam an must use only to add user to the simulator.
     */
    @PostMapping("/testing-support/accounts")
    public IdamUserAddReponse addNewUser(@RequestBody IdamTestingUser request) {
        String userId = UUID.nameUUIDFromBytes(request.getEmail().getBytes()).toString();
        LOG.info("Add new user in simulator for {} {} {}",
                 request.getEmail(), request.getSurname(), userId
        );

        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idam Simulator: email is missing or null");
        }

        liveMemoryService.putSimObject(userId, SimObject.builder()
            .email(request.getEmail())
            .surname(request.getSurname())
            .forename(request.getForename())
            .id(userId)
            .roles(request.getRoles().stream().map(RoleDetails::getCode).collect(Collectors.toList()))
            .build());
        return new IdamUserAddReponse(userId);
    }

}
