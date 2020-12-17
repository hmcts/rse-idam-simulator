package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    //http://localhost:5556/login?redirect_uri=toto&client_id=oneClientId&state=12345&ui_local=en
    @GetMapping("/login")
    public String greeting(Model model,
                           @RequestParam("redirect_uri") String redirectUri,
                           @RequestParam("client_id") String clientId,
                           @RequestParam("state") String state,
                           @RequestParam("ui_local") String uiLocal) {
        String loginFormAction = "/login?redirect_uri=" + redirectUri
            + "&client_id=" + clientId
            + "&ui_Local=" + uiLocal
            + "&response_type=code"
            + "&state=" + state;
        LOG.info("Setup login form with loginFormAction {}", loginFormAction);
        model.addAttribute("loginFormAction", loginFormAction);
        return "login";
    }

}
