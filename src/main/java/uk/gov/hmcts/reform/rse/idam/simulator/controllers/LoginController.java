package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.UserService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings({"PMD.UseObjectForClearerAPI", "PMD.DataflowAnomalyAnalysis"})
@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    private static final String IDAM_SESSION_COOKIE = "Idam.Session";

    @Autowired
    private SimulatorService simulatorService;

    @Autowired
    private UserService userService;

    @Value("${simulator.jwt.issuer}")
    private String jwtIssuer;

    // Show only the quick login accounts, without the username and password form, e.g. for demos.
    @Value("${simulator.login.quick-login-only:false}")
    private boolean quickLoginOnly;

    /*
    Example of a call : http://localhost:5556/login?redirect_uri=toto&client_id=oneClientId&state=12345&ui_local=en
    */
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam("redirect_uri") String redirectUri,
                            @RequestParam("client_id") String clientId,
                            @RequestParam(value = "state", required = false) String state,
                            @RequestParam(value = "nonce", required = false) String nonce,
                            @RequestParam(name = "ui_local", defaultValue = "en") String uiLocal,
                            @CookieValue(name = IDAM_SESSION_COOKIE, required = false) String idamSession) {
        // Like IDAM, a browser that has already logged in is signed straight in to the next service that asks.
        Optional<String> sessionUser = simulatorService.getIdamSessionUser(idamSession);
        if (sessionUser.isPresent()) {
            LOG.info("Signing in {} from their existing session", sessionUser.get());
            return "redirect:" + callbackLocation(sessionUser.get(), redirectUri, clientId, state, nonce);
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/login")
            .queryParam("client_id", clientId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("ui_local", uiLocal)
            .queryParam("response_type", "code");
        if (nonce != null && !nonce.isBlank()) {
            builder.queryParam("nonce", nonce);
        }
        if (state != null && !state.isBlank()) {
            builder.queryParam("state", state);
        }
        String loginFormAction = builder.build().toUriString();
        LOG.info("Setup login form with loginFormAction {}", loginFormAction);
        model.addAttribute("loginFormAction", loginFormAction);
        // Offer the accounts created for quick login, saving people looking up test users' emails. The simulator
        // doesn't check passwords anyway.
        Map<String, List<SimObject>> accountGroups = userService.getAll().stream()
            .filter(user -> user.isQuickLogin() && user.getEmail() != null)
            .sorted(Comparator.comparing((SimObject user) -> nullToEmpty(user.getQuickLoginLabel()))
                        .thenComparing(user -> nullToEmpty(user.getSurname()))
                        .thenComparing(user -> nullToEmpty(user.getForename()))
                        .thenComparing(SimObject::getEmail))
            .collect(Collectors.groupingBy(
                user -> user.getQuickLoginLabel() == null ? "Other accounts" : user.getQuickLoginLabel(),
                LinkedHashMap::new,
                Collectors.toList()));
        model.addAttribute("accountGroups", accountGroups);
        // Without any quick login accounts there would be nothing to choose from, so show the form after all.
        model.addAttribute("pickerOnly", quickLoginOnly && !accountGroups.isEmpty());
        return "login";
    }

    @PostMapping("/login")
    public ResponseEntity<Object> postLogin(HttpServletRequest request,
                                            // Required to not have redirect_uri value overwritten by the framework,
                                            @RequestParam("password") String password,
                                            @RequestParam("username") String username,
                                            @RequestParam("redirect_uri") String redirectUri,
                                            @RequestParam("client_id") String clientId,
                                            @RequestParam(value = "state", required = false) String state,
                                            @RequestParam("response_type") String responseType,
                                            @RequestParam(value = "nonce", required = false) String nonce,
                                            @RequestParam(name = "ui_local", defaultValue = "en") String uiLocal
    ) {
        LOG.info(
            "Post login with values state: {} redirect_uri: {} response_type: {} client_id: {} username: {}",
            state,
            redirectUri,
            responseType,
            clientId,
            username
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        if (request.getCookies() != null) {
            List<Cookie> cookies = Arrays.asList(request.getCookies());
            cookies.forEach(c -> {
                String setCookieHeader = toSetCookieHeader(c);
                httpHeaders.add(HttpHeaders.SET_COOKIE, setCookieHeader);
                LOG.info("Reset cookie {}", setCookieHeader);
            });
        }
        String email = username.trim();
        String locationValue = callbackLocation(email, redirectUri, clientId, state, nonce);
        String newIdamSession = simulatorService.createIdamSession(email);

        httpHeaders.add(HttpHeaders.SET_COOKIE, IDAM_SESSION_COOKIE + "=" + newIdamSession + "; Path=/; HttpOnly");
        httpHeaders.add(HttpHeaders.SET_COOKIE, "idam_ui_locales=" + uiLocal);

        httpHeaders.add("Location", locationValue);
        LOG.info("Location " + locationValue);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    /**
     * Ends the browser session started at login, then returns to the service if it says where.
     */
    @GetMapping("/o/endSession")
    public ResponseEntity<Object> endSession(
        @RequestParam(value = "post_logout_redirect_uri", required = false) String postLogoutRedirectUri,
        @CookieValue(name = IDAM_SESSION_COOKIE, required = false) String idamSession) {
        simulatorService.endIdamSession(idamSession);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.SET_COOKIE, IDAM_SESSION_COOKIE + "=; Path=/; Max-Age=0; HttpOnly");
        if (postLogoutRedirectUri == null || postLogoutRedirectUri.isBlank()) {
            return new ResponseEntity<>(httpHeaders, HttpStatus.NO_CONTENT);
        }
        httpHeaders.add(HttpHeaders.LOCATION, postLogoutRedirectUri);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    private String callbackLocation(String email, String redirectUri, String clientId, String state, String nonce) {
        String code = simulatorService.geAuthCodeFromUserName(email, nonce);
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(redirectUri)
            .queryParam("code", code)
            .queryParam("client_id", clientId)
            .queryParam("iss", jwtIssuer);
        if (state != null && !state.isBlank()) {
            builder.queryParam("state", state);
        }
        return builder.build().toUriString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toSetCookieHeader(Cookie cookie) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookie.getName(), cookie.getValue());
        if (cookie.getPath() != null && !cookie.getPath().isBlank()) {
            builder.path(cookie.getPath());
        }
        if (cookie.getDomain() != null && !cookie.getDomain().isBlank()) {
            builder.domain(cookie.getDomain());
        }
        if (cookie.getMaxAge() >= 0) {
            builder.maxAge(cookie.getMaxAge());
        }
        if (cookie.getSecure()) {
            builder.secure(true);
        }
        if (cookie.isHttpOnly()) {
            builder.httpOnly(true);
        }
        return builder.build().toString();
    }

    @DeleteMapping("/session/{access_token}")
    public ResponseEntity<Object> logout(@PathVariable("access_token") String accessToken) {

        LOG.info("Logout action for token: {}", accessToken);
        // Services that log out this way expect the next login to show the form, so end the user's browser sessions.
        try {
            userService.getByJwToken(accessToken)
                .ifPresent(user -> simulatorService.endIdamSessions(user.getEmail()));
        } catch (RuntimeException e) {
            LOG.info("Logout token isn't one the simulator issued: {}", e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

}
