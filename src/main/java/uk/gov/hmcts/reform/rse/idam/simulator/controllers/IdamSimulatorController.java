package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.rse.idam.simulator.token.JwTokenGenerator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SuppressWarnings("PMD")
@RestController
@RequestMapping("/idamsim")
public class IdamSimulatorController {

    @Value("${simulator.jwt.issuer}")
    private String issuer;

    @Value("${simulator.jwt.expiration}")
    private long expiration;

    /*Maybe deprecated*/
    @GetMapping("/details")
    public IdamUserDetails getDetails(@RequestHeader(AUTHORIZATION) String authorization) {
        return createUserDetails("NotSureProbablyExtractFromHeader");
    }

    /*Maybe deprecated*/
    @PostMapping("/pin")
    public PinDetails postPin(@RequestHeader(AUTHORIZATION) String authorization) {
        return createPindDetails();
    }

    /*Maybe deprecated*/
    @GetMapping(value = "/pin", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> getPin() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "dummyValue");
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    /*
    This method is no longer acceptable as idam now uses OpenID Connect and /oauth2/authorize endpoint is deprecated.
    */
    @Deprecated
    @PostMapping(value = "/oauth2/authorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> authoriseUser(@RequestHeader(AUTHORIZATION) String authorization,
                                                HttpServletRequest request) {
        Map<String, Object> body = new ConcurrentHashMap<>();
        body.put("code", "dummyValue");
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    @PostMapping(value = "/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> oauth2Token(@RequestHeader("my-number") int myNumber, HttpServletRequest request) {
        return createTokenExchangeResponse();
    }

    @PostMapping(value = "/o/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> getOpenIdToken(HttpServletRequest request) {
        return createToken();
    }

    @GetMapping("/o/userinfo")
    public IdamUserInfo getUserInfo(@RequestHeader(AUTHORIZATION) String authorization) {
        return createUserInfo();
    }

    @GetMapping("/api/v1/users/{userId}")
    public IdamUserDetails getUserDetails(@RequestHeader(AUTHORIZATION) String authorization,
                                          @PathVariable("userId") String userId) {
        return createUserDetails(userId);
    }

    @GetMapping("/api/v1/users")
    public List<IdamUserDetails> getSearchUserDetails(@RequestHeader(AUTHORIZATION) String authorization,
                                                      @RequestParam("query") final String elasticSearchQuery) {
        return createUserDetailsList(elasticSearchQuery);
    }

    private List<IdamUserDetails> createUserDetailsList(String elasticSearchQuery) {
        return Arrays.asList(createUserDetails("aa80aa02-f738-11aa-aaa1-0242aa999999"));
    }

    private IdamUserDetails createUserDetails(String userId) {
        IdamUserDetails userDetails = new IdamUserDetails();
        userDetails.setEmail("test-email@hmcts.net");
        userDetails.setForename("John");
        userDetails.setSurname("Smith");
        userDetails.setRoles(Arrays.asList("role1", "role2"));
        return userDetails;
    }

    private IdamUserInfo createUserInfo() {
        IdamUserInfo idamUserInfo = new IdamUserInfo();
        idamUserInfo.setEmail("test-email@hmcts.net");
        idamUserInfo.setFamilyName("Smith");
        idamUserInfo.setGivenName("John");
        idamUserInfo.setName("Johnny");
        idamUserInfo.setRoles(Arrays.asList("role1", "role2"));
        idamUserInfo.setSub("sub99");
        idamUserInfo.setUid("aa80aa02-f738-11aa-aaa1-0242aa999999");
        return idamUserInfo;
    }

    private ResponseEntity<Object> createToken() {
        Map<String, Object> body = new ConcurrentHashMap<>();
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        String refreshToken = JwTokenGenerator.generateToken(issuer, expiration);
        String idToken = JwTokenGenerator.generateToken(issuer, expiration);
        body.put("access_token", token);
        body.put("token_type", "Bearer");
        body.put("expires_in", expiration);
        body.put("id_token", idToken);
        body.put("scope", "openid profile roles search-user");
        body.put("refresh_token", refreshToken);
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    private ResponseEntity<Object> createTokenExchangeResponse() {
        Map<String, Object> body = new ConcurrentHashMap<>();
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        body.put("access_token", token);
        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(body, httpHeaders, HttpStatus.OK);
    }

    private PinDetails createPindDetails() {
        PinDetails pin = new PinDetails();
        pin.setPin("1234");
        pin.setUserId("NotSureProbablyExtractFromHeader");
        return pin;
    }

}
