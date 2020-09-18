package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Builder
@Getter
public class ExchangeCodeRequest {
    private String code;
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("redirect_uri")
    private String redirectUri;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;

    public ExchangeCodeRequest(
            String code, String grantType, String redirectUri, String clientId, String clientSecret
    ) {
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
}
