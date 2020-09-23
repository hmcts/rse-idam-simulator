package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Builder
@Getter
public class AuthenticateUserRequest {
    @JsonProperty("response_type")
    private final String responseType;
    @JsonProperty("client_id")
    private final String clientId;
    @JsonProperty("redirect_uri")
    private final String redirectUri;

    public AuthenticateUserRequest(String responseType, String clientId, String redirectUri) {
        this.responseType = responseType;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

}
