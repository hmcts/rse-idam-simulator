package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

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

    public String getResponseType() {
        return responseType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
