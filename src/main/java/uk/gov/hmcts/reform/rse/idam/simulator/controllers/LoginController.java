package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;


@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

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

    //carry on here intersepct the form correctly check the username
    // return
    /*
    * if (loginSuccess) {
                        log.info("/login: Successful login - {}", obfuscateEmailAddress(request.getUsername()));
                        List<String> secureCookies = authHelper.makeCookiesSecure(cookies);
                        secureCookies.forEach(cookie -> response.addHeader(HttpHeaders.SET_COOKIE, cookie));
                        return new ModelAndView(REDIRECT_PREFIX + responseUrl);
                    }*/


    @PostMapping(value = "/login")
    public String postLogin(HttpServletRequest request,
                            @RequestParam("password") String password,
                            @RequestParam("username") String username,
                            @RequestParam("redirect_uri") String redirectUri,
                            @RequestParam("client_id") String clientId,
                            @RequestParam("state") String state,
                            @RequestParam("response_type") String responseType,
                            @RequestParam("ui_local") String uiLocal
          ) {
        LOG.info("Post login with values state: {} redirect_uri: {} response_type: {} client_id: {} username: {}", state, redirectUri, responseType, clientId, username);
        return "todo";
    }

}
