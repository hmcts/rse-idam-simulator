package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneratePinRequest {

    private final String firstName;
    private final String lastName;

    public GeneratePinRequest(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
