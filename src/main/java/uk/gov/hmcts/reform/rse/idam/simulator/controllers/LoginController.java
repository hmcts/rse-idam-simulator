package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String greeting(Model model) {
        model.addAttribute("name", "tototo");
        return "login";
    }

}
