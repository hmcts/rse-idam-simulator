package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;

import java.util.Arrays;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;



@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private SimulatorService simulatorService;

    @Value("${simulator.jwt.issuer}")
    private String jwtIssuer;

    /*
    Example of a call : http://localhost:5556/login?redirect_uri=toto&client_id=oneClientId&state=12345&ui_local=en
    */
    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam("redirect_uri") String redirectUri,
                            @RequestParam("client_id") String clientId,
                            @RequestParam("state") String state,
                            @RequestParam("ui_local") String uiLocal) {
        String loginFormAction = "/login?"
            + "client_id=" + clientId
            + "&redirect_uri=" + redirectUri
            + "&ui_local=" + uiLocal
            + "&response_type=code"
            + "&state=" + state;
        LOG.info("Setup login form with loginFormAction {}", loginFormAction);
        model.addAttribute("loginFormAction", loginFormAction);
        return "login";
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Object> postLogin(HttpServletRequest request,
                                            // Required to not have redirect_uri value overwritten by the framework,
                                            @RequestParam("password") String password,
                                            @RequestParam("username") String username,
                                            @RequestParam("redirect_uri") String redirectUri,
                                            @RequestParam("client_id") String clientId,
                                            @RequestParam("state") String state,
                                            @RequestParam("response_type") String responseType,
                                            @RequestParam("ui_local") String uiLocal
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
                httpHeaders.add(HttpHeaders.SET_COOKIE, c.toString());
                LOG.info("Reset cookie {}", c.toString());
            });
        }
        String newIdamSession = simulatorService.getNewIdamSessionValue();

        httpHeaders.add(HttpHeaders.SET_COOKIE, "Idam.Session=" + newIdamSession);
        httpHeaders.add(HttpHeaders.SET_COOKIE, "idam_ui_locales=" + uiLocal);

        String code = simulatorService.geAuthCodeFromUserName(username);
        String locationValue = redirectUri
            + "?code=" + code
            + "&state=" + state
            + "&client_id=" + clientId
            + "&iss=" + jwtIssuer;

        httpHeaders.add("Location", locationValue);
        LOG.info("Location " + locationValue);
        return new ResponseEntity<>(httpHeaders, HttpStatus.FOUND);
    }

    //clean code
    //update read me with an example
    //test the read me
    //merge it
    //Ask someone to use it

}
