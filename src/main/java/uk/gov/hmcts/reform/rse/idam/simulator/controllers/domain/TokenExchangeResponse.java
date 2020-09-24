package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenExchangeResponse {

    public final String accessToken;

    public TokenExchangeResponse(@JsonProperty("access_token") String accessToken) {
        this.accessToken = accessToken;
    }

}
