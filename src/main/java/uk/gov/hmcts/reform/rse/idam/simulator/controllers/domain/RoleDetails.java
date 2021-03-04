package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

public class RoleDetails {

    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static RoleDetails build(String code) {
        RoleDetails roleDetails = new RoleDetails();
        roleDetails.setCode(code);
        return roleDetails;
    }
}
