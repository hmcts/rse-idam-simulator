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
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserAddReponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserInfo;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenExchangeResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.TokenResponse;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.rse.idam.simulator.controllers.SimulatorDataFactory.getUserOne;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports",
    "PMD.UseObjectForClearerAPI"})
@RestController
public class IdamSimulatorController {

    private static final Logger LOG = LoggerFactory.getLogger(IdamSimulatorController.class);
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    @Autowired
    private SimulatorService simulatorService;

    @Autowired
    private LiveMemoryService liveMemoryService;

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
        checkUserAuthenticatedByAuthBasic(authorization);

        byte[] decoded = Base64.getDecoder().decode(authorization.replace("Basic ", ""));
        String username = new String(decoded).split(":")[0];

        String newCode = simulatorService.generateOauth2Code(username);
        LOG.info("Request oauth2 new code generated {}", newCode);
        return new AuthenticateUserResponse(newCode);
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenExchangeResponse oauth2Token(@RequestParam(CLIENT_ID) final String clientId,
                                             @RequestParam(REDIRECT_URI) final String redirectUri,
                                             @RequestParam("client_secret") final String clientSecret,
                                             @RequestParam("grant_type") final String grantType,
                                             @RequestParam("code") final String code) {
        LOG.info("Request oauth2 token for code {} and clientId {}", code, clientId);

        String token = simulatorService.generateAuthTokenFromCode(code);

        return new TokenExchangeResponse(token);
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

        String token = simulatorService.generateAToken();
        String refreshToken = simulatorService.generateAToken();
        String idToken = simulatorService.generateAToken();
        LOG.info("Access Open Id Token Generated {}", token);
        simulatorService.updateTokenInUser(username, token);
        return new TokenResponse(token, String.valueOf(expiration), idToken, refreshToken,
                                 "openid profile roles search-user", "Bearer"
        );
    }

    @PostMapping("/pin")
    public PinDetails postPin(@RequestBody GeneratePinRequest request,
                              @RequestHeader(AUTHORIZATION) String authorization) {
        LOG.info("Post Request Pin for {}", request.getFirstName());
        checkUserHasBeenAuthenticateByBearerToken(authorization);
        String userId = liveMemoryService.getByBearerToken(authorization).get().getId();
        return createPinDetails(userId);
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
        checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByBearerToken(authorization).get();
        return toUserDetails(simObject);
    }

    @GetMapping("/o/userinfo")
    public IdamUserInfo getUserInfo(@RequestHeader(AUTHORIZATION) String authorization) {
        checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByBearerToken(authorization).get();
        return toUserInfo(simObject);
    }

    @GetMapping("/api/v1/users/{userId}")
    public IdamUserDetails getUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @PathVariable("userId") String userId) {
        LOG.info("Request User Details {}", userId);
        checkUserHasBeenAuthenticateByBearerToken(authorization);
        SimObject simObject = liveMemoryService.getByUserId(userId);
        return toUserDetails(simObject);
    }

    @GetMapping("/api/v1/users")
    public List<IdamUserDetails> getSearchUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestParam("query") final String elasticSearchQuery) {
        LOG.info("Request Search user details with query {}", elasticSearchQuery);
        checkUserHasBeenAuthenticateByBearerToken(authorization);
        return createUserDetailsList();
    }

    private IdamUserInfo toUserInfo(SimObject simObject) {
        IdamUserInfo idamUserInfo = new IdamUserInfo();
        idamUserInfo.setFamilyName(simObject.getSurname());
        idamUserInfo.setGivenName(simObject.getForename());
        idamUserInfo.setUid(simObject.getId());
        idamUserInfo.setName(simObject.getForename() + " " + simObject.getSurname());
        idamUserInfo.setEmail(simObject.getEmail());
        idamUserInfo.setSub(simObject.getSub());
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

    private PinDetails createPinDetails(String userId) {
        PinDetails pin = new PinDetails();
        pin.setPin("1234");
        pin.setUserId(userId);
        return pin;
    }

    private void checkUserAuthenticatedByAuthBasic(String authorization) {
        if (!authorization.startsWith("Basic")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Basic Auth Token required");
        }
    }

    private void checkUserHasBeenAuthenticateByBearerToken(String authorization) {
        if (!authorization.startsWith("Bearer")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Bearer must start by Bearer");
        }
        if (liveMemoryService.getByBearerToken(authorization).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: User not authenticated");
        }
    }


    @PostMapping("/simulator/user")
    public IdamUserAddReponse addNewUser(@RequestBody IdamUserInfo request) {
        LOG.info("Add new user in simulator for {} {} {}",
                 request.getEmail(), request.getFamilyName(), request.getUid()
        );
        if (request.getUid() == null) {
            request.setUid(UUID.randomUUID().toString());
            LOG.info("UUID generated in simulator for new user {} {}", request.getEmail(), request.getUid());
        }
        liveMemoryService.putSimObject(request.getUid(), SimObject.builder()
            .email(request.getEmail())
            .surname(request.getFamilyName())
            .forename(request.getGivenName())
            .id(request.getUid())
            .roles(request.getRoles())
            .sub(request.getSub()).build());
        return new IdamUserAddReponse(request.getUid());
    }


}
