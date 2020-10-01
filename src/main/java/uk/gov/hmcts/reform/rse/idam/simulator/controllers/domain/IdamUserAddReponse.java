package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class IdamUserAddReponse {

    private String uid;

    @JsonCreator
    public IdamUserAddReponse(@JsonProperty("uuid") String uid) {
        this.uid = uid;
    }
}
