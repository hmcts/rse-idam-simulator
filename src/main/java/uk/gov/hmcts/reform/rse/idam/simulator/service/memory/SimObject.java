package uk.gov.hmcts.reform.rse.idam.simulator.service.memory;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@EqualsAndHashCode
@Builder
@Getter
public class SimObject {

    public static final String BEARER_ = "Bearer ";

    private String clientId;
    /**
     * uid is id in Idam.
     **/
    private String id;
    /**
     * email is also the username in Idam.
     **/
    private String email;
    /**
     * name is usually Forename + Surname in Idam.
     **/
    private String forename;
    private String surname;
    private List<String> roles;
    private String sub;
    private String mostRecentJwToken;
    private long mostRecentJwTokenUnixTime;
    private String mostRecentCode;
    private String lastGeneratedPin;

    public void setMostRecentJwToken(String mostRecentJwToken) {
        this.mostRecentJwToken = mostRecentJwToken.replace(BEARER_, "");
        this.mostRecentJwTokenUnixTime = System.currentTimeMillis();
    }

    public void setMostRecentCode(String mostRecentCode) {
        this.mostRecentCode = mostRecentCode;
    }

    public void setLastGeneratedPin(String lastGeneratedPin) {
        this.lastGeneratedPin = lastGeneratedPin;
    }
}
