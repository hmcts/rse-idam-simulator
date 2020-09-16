package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest
public class SimulatorHappyPathTest {

    @Autowired
    private transient MockMvc mockMvc;

    @DisplayName("Should return expected user")
    @Test
    public void returnUserDetails() throws Exception {
        mockMvc.perform(get("/api/v1/users/"+123).header("Authorization", "Bearer foo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123"))
            .andExpect(jsonPath("$.email").value("test-email@hmcts.net"))
            .andExpect(jsonPath("$.forename").value("John"))
            .andExpect(jsonPath("$.surname").value("Smith"))
            .andExpect(jsonPath("$.roles[0]").value("role1"))
            .andExpect(jsonPath("$.roles[1]").value("role2"))
            .andReturn();
    }
}
