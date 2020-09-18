package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GeneratePinRequest {

    private final String firstName;
    private final String lastName;
    private final List<String> roles;

    public GeneratePinRequest(String name) {
        this.firstName = name;
        this.lastName = "";
        this.roles = Collections.emptyList();
    }
}
