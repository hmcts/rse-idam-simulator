package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamUserDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;

import java.util.Arrays;

public final class SimulatorDataFactory {

    public static final String SMITH = "Smith";
    public static final String JOHN = "John";
    public static final String TEST_EMAIL_HMCTS_NET = "test-email@hmcts.net";
    public static final String ROLE_1 = "role1";
    public static final String ROLE_2 = "role2";
    public static final String CLIENT_ID = "hmcts";

    private SimulatorDataFactory() {
    }

    public static IdamUserDetails getUserOne(String userId) {
        IdamUserDetails userDetails = new IdamUserDetails();
        userDetails.setId(userId);
        userDetails.setEmail(TEST_EMAIL_HMCTS_NET);
        userDetails.setForename(JOHN);
        userDetails.setSurname(SMITH);
        userDetails.setRoles(Arrays.asList(ROLE_1, ROLE_2));
        return userDetails;
    }

    public static SimObject createSimObject() {
        return SimObject.builder()
            .surname(SMITH)
            .clientId(CLIENT_ID)
            .forename(JOHN)
            .email(TEST_EMAIL_HMCTS_NET)
            .sub("oneSub")
            .id("oneUserId")
            .roles(Arrays.asList(ROLE_1, ROLE_2))
            .build();
    }
}
