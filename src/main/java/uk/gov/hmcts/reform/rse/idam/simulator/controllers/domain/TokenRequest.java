package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TokenRequest {

    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("client_secret")
    private String clientSecret;
    @JsonProperty("grant_type")
    private String grantType;
    @JsonProperty("redirect_uri")
    private String redirectUri;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("scope")
    private String scope;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("code")
    private String code;

    public TokenRequest(
        String clientId,
        String clientSecret,
        String grantType,
        String redirectUri,
        String username,
        String password,
        String scope,
        String refreshToken,
        String code
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
        this.username = username;
        this.password = password;
        this.scope = scope;
        this.refreshToken = refreshToken;
        this.code = code;
    }

}


