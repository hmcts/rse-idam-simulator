package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamTestingUser;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.RoleDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.PersistentStorageService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {
    IdamSimulatorController.class,
    LoginController.class,
    LiveMemoryService.class,
    PersistentStorageService.class,
    SimulatorService.class,
    JsonWebKeyService.class,
    JwTokenGeneratorService.class,
    OpenIdConfigService.class
    }, properties = {"spring.main.lazy-initialization=true", "simulator.login.quick-login-only=true"})
@AutoConfigureMockMvc
@PropertySource("classpath:application.yaml")
@EnableAutoConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class QuickLoginOnlySpringBootTest {

    // The username and password form, still in the page (for the picker's own submit and tests) but hidden.
    private static final String HIDDEN_FORM = "(?s).*<div class=\"container\" hidden=\"hidden\">.*";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void showsOnlyThePickerWhenThereAreQuickLoginAccounts() throws Exception {
        addQuickLoginUser("judge@example.com");
        mockMvc.perform(get("/login").param("client_id", "hmcts").param("redirect_uri", "https://localhost/receiver"))
            .andExpect(status().isOk())
            .andExpect(content().string(matchesPattern(HIDDEN_FORM)))
            .andExpect(content().string(containsString("id=\"username\"")))
            .andExpect(content().string(containsString("Choose who to sign in as")))
            .andExpect(content().string(
                containsString("<center hidden=\"hidden\"> <h1> RSE Idam Simulator Login Form")))
            .andExpect(content().string(containsString("data-email=\"judge@example.com\"")));
    }

    @Test
    void showsTheFormWhenThereAreNoQuickLoginAccounts() throws Exception {
        mockMvc.perform(get("/login").param("client_id", "hmcts").param("redirect_uri", "https://localhost/receiver"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(matchesPattern(HIDDEN_FORM))))
            .andExpect(content().string(containsString("<center> <h1> RSE Idam Simulator Login Form")))
            .andExpect(content().string(containsString("id=\"username\"")));
    }

    private void addQuickLoginUser(String email) throws Exception {
        IdamTestingUser user = new IdamTestingUser();
        user.setEmail(email);
        user.setForename("Sam");
        user.setSurname("Judge");
        user.setPassword("OnePassword");
        user.setRoles(List.of(RoleDetails.build("judge")));
        user.setQuickLogin(true);
        user.setQuickLoginLabel("District Judge");
        mockMvc.perform(post("/testing-support/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
            .andExpect(status().isOk());
    }
}
