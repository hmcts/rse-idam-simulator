package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import java.util.Collections;
import java.util.List;

public class IdamTestingUser {

    private String email;
    private String password;
    private String forename;
    private String surname;
    private List<RoleDetails> roles = Collections.emptyList();
    /**
     * Whether the login page offers this account for quick login. Off by default, so system accounts stay hidden.
     */
    private boolean quickLogin;
    /**
     * How the login page describes the account, e.g. "District Judge".
     */
    private String quickLoginLabel;

    public boolean isQuickLogin() {
        return quickLogin;
    }

    public void setQuickLogin(boolean quickLogin) {
        this.quickLogin = quickLogin;
    }

    public String getQuickLoginLabel() {
        return quickLoginLabel;
    }

    public void setQuickLoginLabel(String quickLoginLabel) {
        this.quickLoginLabel = quickLoginLabel;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<RoleDetails> getRoles() {
        return this.roles;
    }

    public void setRoles(List<RoleDetails> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
