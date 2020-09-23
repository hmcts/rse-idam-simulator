package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.AuthenticateUserResponse;

public class SimulatorDataFactory {

    private static final String OAUTH2_USER_PASSWORD_CODE = "eEdhNnasWy7eNFAV";

    public static final AuthenticateUserResponse OAUTH2_USER_PASSWORD_RESPONSE = new AuthenticateUserResponse(OAUTH2_USER_PASSWORD_CODE);
}
