package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"PMD","checkstyle:MemberName","checkstyle:MethodName"})
public class OpenIdConfig {
    @JsonProperty("request_parameter_supported")
    private Boolean requestParameterSupported = false;
    @JsonProperty("claims_parameter_supported")
    private Boolean claimsParameterSupported = false;
    @JsonProperty("introspection_endpoint")
    private String introspectionEndpoint = null;
    @JsonProperty("check_session_iframe")
    private String checkSessionIframe = null;
    @JsonProperty("scopes_supported")
    private List<String> scopesSupported = null;
    @JsonProperty("issuer")
    private String issuer = null;
    @JsonProperty("id_token_encryption_enc_values_supported")
    private List<String> idTokenEncryptionEncValuesSupported = null;
    @JsonProperty("acr_values_supported")
    private List<String> acrValuesSupported = null;
    @JsonProperty("authorization_endpoint")
    private String authorizationEndpoint = null;
    @JsonProperty("request_object_encryption_enc_values_supported")
    private List<String> requestObjectEncryptionEncValuesSupported = null;
    @JsonProperty("rcs_request_encryption_alg_values_supported")
    private List<String> rcsRequestEncryptionAlgValuesSupported = null;
    @JsonProperty("claims_supported")
    private List<String> claimsSupported = null;
    @JsonProperty("rcs_request_signing_alg_values_supported")
    private List<String> rcsRequestSigningAlgValuesSupported = null;
    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported = null;
    @JsonProperty("token_endpoint")
    private String tokenEndpoint = null;
    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported = null;
    @JsonProperty("request_uri_parameter_supported")
    private Boolean requestUriParameterSupported = false;
    @JsonProperty("rcs_response_encryption_enc_values_supported")
    private List<String> rcsResponseEncryptionEncValuesSupported = null;
    @JsonProperty("end_session_endpoint")
    private String endSessionEndpoint = "";
    @JsonProperty("rcs_request_encryption_enc_values_supported")
    private List<String> rcsRequestEncryptionEncValuesSupported = null;
    @JsonProperty("version")
    private String version = null;
    @JsonProperty("rcs_response_encryption_alg_values_supported")
    private List<String> rcsResponseEncryptionAlgValuesSupported = null;
    @JsonProperty("userinfo_endpoint")
    private String userinfoEndpoint = null;
    @JsonProperty("id_token_encryption_alg_values_supported")
    private List<String> idTokenEncryptionAlgValuesSupported = null;
    @JsonProperty("jwks_uri")
    private String jwksUri = null;
    @JsonProperty("subject_types_supported")
    private List<String> subjectTypesSupported = null;
    @JsonProperty("id_token_signing_alg_values_supported")
    private List<String> idTokenSigningAlgValuesSupported = null;
    @JsonProperty("registration_endpoint")
    private String registrationEndpoint = null;
    @JsonProperty("request_object_signing_alg_values_supported")
    private List<String> requestObjectSigningAlgValuesSupported = null;
    @JsonProperty("request_object_encryption_alg_values_supported")
    private List<String> requestObjectEncryptionAlgValuesSupported = null;
    @JsonProperty("rcs_response_signing_alg_values_supported")
    private List<String> rcsResponseSigningAlgValuesSupported = null;

    public OpenIdConfig() {
    }

    public OpenIdConfig requestParameterSupported(Boolean requestParameterSupported) {
        this.requestParameterSupported = requestParameterSupported;
        return this;
    }

    @ApiModelProperty("")
    public Boolean isRequestParameterSupported() {
        return this.requestParameterSupported;
    }

    public void setRequestParameterSupported(Boolean requestParameterSupported) {
        this.requestParameterSupported = requestParameterSupported;
    }

    public OpenIdConfig claimsParameterSupported(Boolean claimsParameterSupported) {
        this.claimsParameterSupported = claimsParameterSupported;
        return this;
    }

    @ApiModelProperty("")
    public Boolean isClaimsParameterSupported() {
        return this.claimsParameterSupported;
    }

    public void setClaimsParameterSupported(Boolean claimsParameterSupported) {
        this.claimsParameterSupported = claimsParameterSupported;
    }

    public OpenIdConfig introspectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getIntrospectionEndpoint() {
        return this.introspectionEndpoint;
    }

    public void setIntrospectionEndpoint(String introspectionEndpoint) {
        this.introspectionEndpoint = introspectionEndpoint;
    }

    public OpenIdConfig checkSessionIframe(String checkSessionIframe) {
        this.checkSessionIframe = checkSessionIframe;
        return this;
    }

    @ApiModelProperty("")
    public String getCheckSessionIframe() {
        return this.checkSessionIframe;
    }

    public void setCheckSessionIframe(String checkSessionIframe) {
        this.checkSessionIframe = checkSessionIframe;
    }

    public OpenIdConfig scopesSupported(List<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
        return this;
    }

    public OpenIdConfig addScopesSupportedItem(String scopesSupportedItem) {
        if (this.scopesSupported == null) {
            this.scopesSupported = new ArrayList<String>();
        }

        this.scopesSupported.add(scopesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getScopesSupported() {
        return this.scopesSupported;
    }

    public void setScopesSupported(List<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    public OpenIdConfig issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    @ApiModelProperty("")
    public String getIssuer() {
        return this.issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public OpenIdConfig idTokenEncryptionEncValuesSupported(List<String> idTokenEncryptionEncValuesSupported) {
        this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
        return this;
    }

    public OpenIdConfig addIdTokenEncryptionEncValuesSupportedItem(
        String idTokenEncryptionEncValuesSupportedItem) {
        if (this.idTokenEncryptionEncValuesSupported == null) {
            this.idTokenEncryptionEncValuesSupported = new ArrayList<String>();
        }

        this.idTokenEncryptionEncValuesSupported.add(idTokenEncryptionEncValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getIdTokenEncryptionEncValuesSupported() {
        return this.idTokenEncryptionEncValuesSupported;
    }

    public void setIdTokenEncryptionEncValuesSupported(List<String> idTokenEncryptionEncValuesSupported) {
        this.idTokenEncryptionEncValuesSupported = idTokenEncryptionEncValuesSupported;
    }

    public OpenIdConfig acrValuesSupported(List<String> acrValuesSupported) {
        this.acrValuesSupported = acrValuesSupported;
        return this;
    }

    public OpenIdConfig addAcrValuesSupportedItem(String acrValuesSupportedItem) {
        if (this.acrValuesSupported == null) {
            this.acrValuesSupported = new ArrayList<String>();
        }

        this.acrValuesSupported.add(acrValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getAcrValuesSupported() {
        return this.acrValuesSupported;
    }

    public void setAcrValuesSupported(List<String> acrValuesSupported) {
        this.acrValuesSupported = acrValuesSupported;
    }

    public OpenIdConfig authorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getAuthorizationEndpoint() {
        return this.authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public OpenIdConfig requestObjectEncryptionEncValuesSupported(
        List<String> requestObjectEncryptionEncValuesSupported) {
        this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
        return this;
    }

    public OpenIdConfig addRequestObjectEncryptionEncValuesSupportedItem(
        String requestObjectEncryptionEncValuesSupportedItem) {
        if (this.requestObjectEncryptionEncValuesSupported == null) {
            this.requestObjectEncryptionEncValuesSupported = new ArrayList<String>();
        }

        this.requestObjectEncryptionEncValuesSupported.add(requestObjectEncryptionEncValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRequestObjectEncryptionEncValuesSupported() {
        return this.requestObjectEncryptionEncValuesSupported;
    }

    public void setRequestObjectEncryptionEncValuesSupported(List<String> requestObjectEncryptionEncValuesSupported) {
        this.requestObjectEncryptionEncValuesSupported = requestObjectEncryptionEncValuesSupported;
    }

    public OpenIdConfig rcsRequestEncryptionAlgValuesSupported(
        List<String> rcsRequestEncryptionAlgValuesSupported) {
        this.rcsRequestEncryptionAlgValuesSupported = rcsRequestEncryptionAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsRequestEncryptionAlgValuesSupportedItem(
        String rcsRequestEncryptionAlgValuesSupportedItem) {
        if (this.rcsRequestEncryptionAlgValuesSupported == null) {
            this.rcsRequestEncryptionAlgValuesSupported = new ArrayList<String>();
        }

        this.rcsRequestEncryptionAlgValuesSupported.add(rcsRequestEncryptionAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsRequestEncryptionAlgValuesSupported() {
        return this.rcsRequestEncryptionAlgValuesSupported;
    }

    public void setRcsRequestEncryptionAlgValuesSupported(List<String> rcsRequestEncryptionAlgValuesSupported) {
        this.rcsRequestEncryptionAlgValuesSupported = rcsRequestEncryptionAlgValuesSupported;
    }

    public OpenIdConfig claimsSupported(List<String> claimsSupported) {
        this.claimsSupported = claimsSupported;
        return this;
    }

    public OpenIdConfig addClaimsSupportedItem(String claimsSupportedItem) {
        if (this.claimsSupported == null) {
            this.claimsSupported = new ArrayList<String>();
        }

        this.claimsSupported.add(claimsSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getClaimsSupported() {
        return this.claimsSupported;
    }

    public void setClaimsSupported(List<String> claimsSupported) {
        this.claimsSupported = claimsSupported;
    }

    public OpenIdConfig rcsRequestSigningAlgValuesSupported(List<String> rcsRequestSigningAlgValuesSupported) {
        this.rcsRequestSigningAlgValuesSupported = rcsRequestSigningAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsRequestSigningAlgValuesSupportedItem(
        String rcsRequestSigningAlgValuesSupportedItem) {
        if (this.rcsRequestSigningAlgValuesSupported == null) {
            this.rcsRequestSigningAlgValuesSupported = new ArrayList<String>();
        }

        this.rcsRequestSigningAlgValuesSupported.add(rcsRequestSigningAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsRequestSigningAlgValuesSupported() {
        return this.rcsRequestSigningAlgValuesSupported;
    }

    public void setRcsRequestSigningAlgValuesSupported(List<String> rcsRequestSigningAlgValuesSupported) {
        this.rcsRequestSigningAlgValuesSupported = rcsRequestSigningAlgValuesSupported;
    }

    public OpenIdConfig tokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
        return this;
    }

    public OpenIdConfig addTokenEndpointAuthMethodsSupportedItem(String tokenEndpointAuthMethodsSupportedItem) {
        if (this.tokenEndpointAuthMethodsSupported == null) {
            this.tokenEndpointAuthMethodsSupported = new ArrayList<String>();
        }

        this.tokenEndpointAuthMethodsSupported.add(tokenEndpointAuthMethodsSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getTokenEndpointAuthMethodsSupported() {
        return this.tokenEndpointAuthMethodsSupported;
    }

    public void setTokenEndpointAuthMethodsSupported(List<String> tokenEndpointAuthMethodsSupported) {
        this.tokenEndpointAuthMethodsSupported = tokenEndpointAuthMethodsSupported;
    }

    public OpenIdConfig tokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getTokenEndpoint() {
        return this.tokenEndpoint;
    }

    public void setTokenEndpoint(String tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public OpenIdConfig responseTypesSupported(List<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
        return this;
    }

    public OpenIdConfig addResponseTypesSupportedItem(String responseTypesSupportedItem) {
        if (this.responseTypesSupported == null) {
            this.responseTypesSupported = new ArrayList<String>();
        }

        this.responseTypesSupported.add(responseTypesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getResponseTypesSupported() {
        return this.responseTypesSupported;
    }

    public void setResponseTypesSupported(List<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public OpenIdConfig requestUriParameterSupported(Boolean requestUriParameterSupported) {
        this.requestUriParameterSupported = requestUriParameterSupported;
        return this;
    }

    @ApiModelProperty("")
    public Boolean isRequestUriParameterSupported() {
        return this.requestUriParameterSupported;
    }

    public void setRequestUriParameterSupported(Boolean requestUriParameterSupported) {
        this.requestUriParameterSupported = requestUriParameterSupported;
    }

    public OpenIdConfig rcsResponseEncryptionEncValuesSupported(
        List<String> rcsResponseEncryptionEncValuesSupported) {
        this.rcsResponseEncryptionEncValuesSupported = rcsResponseEncryptionEncValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsResponseEncryptionEncValuesSupportedItem(
        String rcsResponseEncryptionEncValuesSupportedItem) {
        if (this.rcsResponseEncryptionEncValuesSupported == null) {
            this.rcsResponseEncryptionEncValuesSupported = new ArrayList<String>();
        }

        this.rcsResponseEncryptionEncValuesSupported.add(rcsResponseEncryptionEncValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsResponseEncryptionEncValuesSupported() {
        return this.rcsResponseEncryptionEncValuesSupported;
    }

    public void setRcsResponseEncryptionEncValuesSupported(List<String> rcsResponseEncryptionEncValuesSupported) {
        this.rcsResponseEncryptionEncValuesSupported = rcsResponseEncryptionEncValuesSupported;
    }

    public OpenIdConfig endSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getEndSessionEndpoint() {
        return this.endSessionEndpoint;
    }

    public void setEndSessionEndpoint(String endSessionEndpoint) {
        this.endSessionEndpoint = endSessionEndpoint;
    }

    public OpenIdConfig rcsRequestEncryptionEncValuesSupported(
        List<String> rcsRequestEncryptionEncValuesSupported) {
        this.rcsRequestEncryptionEncValuesSupported = rcsRequestEncryptionEncValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsRequestEncryptionEncValuesSupportedItem(
        String rcsRequestEncryptionEncValuesSupportedItem) {
        if (this.rcsRequestEncryptionEncValuesSupported == null) {
            this.rcsRequestEncryptionEncValuesSupported = new ArrayList<String>();
        }

        this.rcsRequestEncryptionEncValuesSupported.add(rcsRequestEncryptionEncValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsRequestEncryptionEncValuesSupported() {
        return this.rcsRequestEncryptionEncValuesSupported;
    }

    public void setRcsRequestEncryptionEncValuesSupported(List<String> rcsRequestEncryptionEncValuesSupported) {
        this.rcsRequestEncryptionEncValuesSupported = rcsRequestEncryptionEncValuesSupported;
    }

    public OpenIdConfig version(String version) {
        this.version = version;
        return this;
    }

    @ApiModelProperty("")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public OpenIdConfig rcsResponseEncryptionAlgValuesSupported(
        List<String> rcsResponseEncryptionAlgValuesSupported) {
        this.rcsResponseEncryptionAlgValuesSupported = rcsResponseEncryptionAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsResponseEncryptionAlgValuesSupportedItem(
        String rcsResponseEncryptionAlgValuesSupportedItem) {
        if (this.rcsResponseEncryptionAlgValuesSupported == null) {
            this.rcsResponseEncryptionAlgValuesSupported = new ArrayList<String>();
        }

        this.rcsResponseEncryptionAlgValuesSupported.add(rcsResponseEncryptionAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsResponseEncryptionAlgValuesSupported() {
        return this.rcsResponseEncryptionAlgValuesSupported;
    }

    public void setRcsResponseEncryptionAlgValuesSupported(List<String> rcsResponseEncryptionAlgValuesSupported) {
        this.rcsResponseEncryptionAlgValuesSupported = rcsResponseEncryptionAlgValuesSupported;
    }

    public OpenIdConfig userinfoEndpoint(String userinfoEndpoint) {
        this.userinfoEndpoint = userinfoEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getUserinfoEndpoint() {
        return this.userinfoEndpoint;
    }

    public void setUserinfoEndpoint(String userinfoEndpoint) {
        this.userinfoEndpoint = userinfoEndpoint;
    }

    public OpenIdConfig idTokenEncryptionAlgValuesSupported(List<String> idTokenEncryptionAlgValuesSupported) {
        this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addIdTokenEncryptionAlgValuesSupportedItem(
        String idTokenEncryptionAlgValuesSupportedItem) {
        if (this.idTokenEncryptionAlgValuesSupported == null) {
            this.idTokenEncryptionAlgValuesSupported = new ArrayList<String>();
        }

        this.idTokenEncryptionAlgValuesSupported.add(idTokenEncryptionAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getIdTokenEncryptionAlgValuesSupported() {
        return this.idTokenEncryptionAlgValuesSupported;
    }

    public void setIdTokenEncryptionAlgValuesSupported(List<String> idTokenEncryptionAlgValuesSupported) {
        this.idTokenEncryptionAlgValuesSupported = idTokenEncryptionAlgValuesSupported;
    }

    public OpenIdConfig jwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
        return this;
    }

    @ApiModelProperty("")
    public String getJwksUri() {
        return this.jwksUri;
    }

    public void setJwksUri(String jwksUri) {
        this.jwksUri = jwksUri;
    }

    public OpenIdConfig subjectTypesSupported(List<String> subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
        return this;
    }

    public OpenIdConfig addSubjectTypesSupportedItem(String subjectTypesSupportedItem) {
        if (this.subjectTypesSupported == null) {
            this.subjectTypesSupported = new ArrayList<String>();
        }

        this.subjectTypesSupported.add(subjectTypesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getSubjectTypesSupported() {
        return this.subjectTypesSupported;
    }

    public void setSubjectTypesSupported(List<String> subjectTypesSupported) {
        this.subjectTypesSupported = subjectTypesSupported;
    }

    public OpenIdConfig idTokenSigningAlgValuesSupported(List<String> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addIdTokenSigningAlgValuesSupportedItem(String idTokenSigningAlgValuesSupportedItem) {
        if (this.idTokenSigningAlgValuesSupported == null) {
            this.idTokenSigningAlgValuesSupported = new ArrayList<String>();
        }

        this.idTokenSigningAlgValuesSupported.add(idTokenSigningAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getIdTokenSigningAlgValuesSupported() {
        return this.idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(List<String> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }

    public OpenIdConfig registrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
        return this;
    }

    @ApiModelProperty("")
    public String getRegistrationEndpoint() {
        return this.registrationEndpoint;
    }

    public void setRegistrationEndpoint(String registrationEndpoint) {
        this.registrationEndpoint = registrationEndpoint;
    }

    public OpenIdConfig requestObjectSigningAlgValuesSupported(
        List<String> requestObjectSigningAlgValuesSupported) {
        this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRequestObjectSigningAlgValuesSupportedItem(
        String requestObjectSigningAlgValuesSupportedItem) {
        if (this.requestObjectSigningAlgValuesSupported == null) {
            this.requestObjectSigningAlgValuesSupported = new ArrayList<String>();
        }

        this.requestObjectSigningAlgValuesSupported.add(requestObjectSigningAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRequestObjectSigningAlgValuesSupported() {
        return this.requestObjectSigningAlgValuesSupported;
    }

    public void setRequestObjectSigningAlgValuesSupported(List<String> requestObjectSigningAlgValuesSupported) {
        this.requestObjectSigningAlgValuesSupported = requestObjectSigningAlgValuesSupported;
    }

    public OpenIdConfig requestObjectEncryptionAlgValuesSupported(
        List<String> requestObjectEncryptionAlgValuesSupported) {
        this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRequestObjectEncryptionAlgValuesSupportedItem(
        String requestObjectEncryptionAlgValuesSupportedItem) {
        if (this.requestObjectEncryptionAlgValuesSupported == null) {
            this.requestObjectEncryptionAlgValuesSupported = new ArrayList<String>();
        }

        this.requestObjectEncryptionAlgValuesSupported.add(requestObjectEncryptionAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRequestObjectEncryptionAlgValuesSupported() {
        return this.requestObjectEncryptionAlgValuesSupported;
    }

    public void setRequestObjectEncryptionAlgValuesSupported(List<String> requestObjectEncryptionAlgValuesSupported) {
        this.requestObjectEncryptionAlgValuesSupported = requestObjectEncryptionAlgValuesSupported;
    }

    public OpenIdConfig rcsResponseSigningAlgValuesSupported(List<String> rcsResponseSigningAlgValuesSupported) {
        this.rcsResponseSigningAlgValuesSupported = rcsResponseSigningAlgValuesSupported;
        return this;
    }

    public OpenIdConfig addRcsResponseSigningAlgValuesSupportedItem(
        String rcsResponseSigningAlgValuesSupportedItem) {
        if (this.rcsResponseSigningAlgValuesSupported == null) {
            this.rcsResponseSigningAlgValuesSupported = new ArrayList<String>();
        }

        this.rcsResponseSigningAlgValuesSupported.add(rcsResponseSigningAlgValuesSupportedItem);
        return this;
    }

    @ApiModelProperty("")
    public List<String> getRcsResponseSigningAlgValuesSupported() {
        return this.rcsResponseSigningAlgValuesSupported;
    }

    public void setRcsResponseSigningAlgValuesSupported(List<String> rcsResponseSigningAlgValuesSupported) {
        this.rcsResponseSigningAlgValuesSupported = rcsResponseSigningAlgValuesSupported;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            OpenIdConfig openIdConfig = (OpenIdConfig) o;
            return Objects.equals(this.requestParameterSupported, openIdConfig.requestParameterSupported)
                && Objects.equals(
                this.claimsParameterSupported,
                openIdConfig.claimsParameterSupported
            ) && Objects.equals(this.introspectionEndpoint, openIdConfig.introspectionEndpoint) && Objects.equals(
                this.checkSessionIframe,
                openIdConfig.checkSessionIframe
            ) && Objects.equals(this.scopesSupported, openIdConfig.scopesSupported) && Objects.equals(
                this.issuer,
                openIdConfig.issuer
            ) && Objects.equals(
                this.idTokenEncryptionEncValuesSupported,
                openIdConfig.idTokenEncryptionEncValuesSupported
            ) && Objects.equals(
                this.acrValuesSupported,
                openIdConfig.acrValuesSupported
            ) && Objects.equals(this.authorizationEndpoint, openIdConfig.authorizationEndpoint) && Objects.equals(
                this.requestObjectEncryptionEncValuesSupported,
                openIdConfig.requestObjectEncryptionEncValuesSupported
            ) && Objects.equals(
                this.rcsRequestEncryptionAlgValuesSupported,
                openIdConfig.rcsRequestEncryptionAlgValuesSupported
            ) && Objects.equals(
                this.claimsSupported,
                openIdConfig.claimsSupported
            ) && Objects.equals(
                this.rcsRequestSigningAlgValuesSupported,
                openIdConfig.rcsRequestSigningAlgValuesSupported
            ) && Objects.equals(
                this.tokenEndpointAuthMethodsSupported,
                openIdConfig.tokenEndpointAuthMethodsSupported
            ) && Objects.equals(
                this.tokenEndpoint,
                openIdConfig.tokenEndpoint
            ) && Objects.equals(
                this.responseTypesSupported,
                openIdConfig.responseTypesSupported
            ) && Objects.equals(
                this.requestUriParameterSupported,
                openIdConfig.requestUriParameterSupported
            ) && Objects.equals(
                this.rcsResponseEncryptionEncValuesSupported,
                openIdConfig.rcsResponseEncryptionEncValuesSupported
            ) && Objects.equals(
                this.endSessionEndpoint,
                openIdConfig.endSessionEndpoint
            ) && Objects.equals(
                this.rcsRequestEncryptionEncValuesSupported,
                openIdConfig.rcsRequestEncryptionEncValuesSupported
            ) && Objects.equals(
                this.version,
                openIdConfig.version
            ) && Objects.equals(
                this.rcsResponseEncryptionAlgValuesSupported,
                openIdConfig.rcsResponseEncryptionAlgValuesSupported
            ) && Objects.equals(
                this.userinfoEndpoint,
                openIdConfig.userinfoEndpoint
            ) && Objects.equals(
                this.idTokenEncryptionAlgValuesSupported,
                openIdConfig.idTokenEncryptionAlgValuesSupported
            ) && Objects.equals(this.jwksUri, openIdConfig.jwksUri) && Objects.equals(
                this.subjectTypesSupported,
                openIdConfig.subjectTypesSupported
            ) && Objects.equals(
                this.idTokenSigningAlgValuesSupported,
                openIdConfig.idTokenSigningAlgValuesSupported
            ) && Objects.equals(this.registrationEndpoint, openIdConfig.registrationEndpoint) && Objects.equals(
                this.requestObjectSigningAlgValuesSupported,
                openIdConfig.requestObjectSigningAlgValuesSupported
            ) && Objects.equals(
                this.requestObjectEncryptionAlgValuesSupported,
                openIdConfig.requestObjectEncryptionAlgValuesSupported
            ) && Objects.equals(
                this.rcsResponseSigningAlgValuesSupported,
                openIdConfig.rcsResponseSigningAlgValuesSupported
            );
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.requestParameterSupported, this.claimsParameterSupported,
            this.introspectionEndpoint, this.checkSessionIframe, this.scopesSupported, this.issuer,
            this.idTokenEncryptionEncValuesSupported, this.acrValuesSupported, this.authorizationEndpoint,
            this.requestObjectEncryptionEncValuesSupported, this.rcsRequestEncryptionAlgValuesSupported,
            this.claimsSupported, this.rcsRequestSigningAlgValuesSupported, this.tokenEndpointAuthMethodsSupported,
            this.tokenEndpoint, this.responseTypesSupported, this.requestUriParameterSupported,
            this.rcsResponseEncryptionEncValuesSupported, this.endSessionEndpoint,
            this.rcsRequestEncryptionEncValuesSupported, this.version, this.rcsResponseEncryptionAlgValuesSupported,
            this.userinfoEndpoint, this.idTokenEncryptionAlgValuesSupported, this.jwksUri, this.subjectTypesSupported,
            this.idTokenSigningAlgValuesSupported, this.registrationEndpoint,
            this.requestObjectSigningAlgValuesSupported, this.requestObjectEncryptionAlgValuesSupported,
            this.rcsResponseSigningAlgValuesSupported});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AmWellKnownConfig {\n");
        sb.append("    requestParameterSupported: ")
            .append(this.toIndentedString(this.requestParameterSupported)).append("\n");
        sb.append("    claimsParameterSupported: ")
            .append(this.toIndentedString(this.claimsParameterSupported)).append("\n");
        sb.append("    introspectionEndpoint: ").append(this.toIndentedString(this.introspectionEndpoint)).append("\n");
        sb.append("    checkSessionIframe: ").append(this.toIndentedString(this.checkSessionIframe)).append("\n");
        sb.append("    scopesSupported: ").append(this.toIndentedString(this.scopesSupported)).append("\n");
        sb.append("    issuer: ").append(this.toIndentedString(this.issuer)).append("\n");
        sb.append("    idTokenEncryptionEncValuesSupported: ")
            .append(this.toIndentedString(this.idTokenEncryptionEncValuesSupported)).append("\n");
        sb.append("    acrValuesSupported: ").append(this.toIndentedString(this.acrValuesSupported)).append("\n");
        sb.append("    authorizationEndpoint: ").append(this.toIndentedString(this.authorizationEndpoint)).append("\n");
        sb.append("    requestObjectEncryptionEncValuesSupported: ")
            .append(this.toIndentedString(this.requestObjectEncryptionEncValuesSupported)).append("\n");
        sb.append("    rcsRequestEncryptionAlgValuesSupported: ")
            .append(this.toIndentedString(this.rcsRequestEncryptionAlgValuesSupported)).append("\n");
        sb.append("    claimsSupported: ").append(this.toIndentedString(this.claimsSupported)).append("\n");
        sb.append("    rcsRequestSigningAlgValuesSupported: ")
            .append(this.toIndentedString(this.rcsRequestSigningAlgValuesSupported)).append("\n");
        sb.append("    tokenEndpointAuthMethodsSupported: ")
            .append(this.toIndentedString(this.tokenEndpointAuthMethodsSupported)).append("\n");
        sb.append("    tokenEndpoint: ").append(this.toIndentedString(this.tokenEndpoint)).append("\n");
        sb.append("    responseTypesSupported: ")
            .append(this.toIndentedString(this.responseTypesSupported)).append("\n");
        sb.append("    requestUriParameterSupported: ")
            .append(this.toIndentedString(this.requestUriParameterSupported)).append("\n");
        sb.append("    rcsResponseEncryptionEncValuesSupported: ")
            .append(this.toIndentedString(this.rcsResponseEncryptionEncValuesSupported)).append("\n");
        sb.append("    endSessionEndpoint: ").append(this.toIndentedString(this.endSessionEndpoint)).append("\n");
        sb.append("    rcsRequestEncryptionEncValuesSupported: ")
            .append(this.toIndentedString(this.rcsRequestEncryptionEncValuesSupported)).append("\n");
        sb.append("    version: ").append(this.toIndentedString(this.version)).append("\n");
        sb.append("    rcsResponseEncryptionAlgValuesSupported: ")
            .append(this.toIndentedString(this.rcsResponseEncryptionAlgValuesSupported)).append("\n");
        sb.append("    userinfoEndpoint: ").append(this.toIndentedString(this.userinfoEndpoint)).append("\n");
        sb.append("    idTokenEncryptionAlgValuesSupported: ")
            .append(this.toIndentedString(this.idTokenEncryptionAlgValuesSupported)).append("\n");
        sb.append("    jwksUri: ").append(this.toIndentedString(this.jwksUri)).append("\n");
        sb.append("    subjectTypesSupported: ").append(this.toIndentedString(this.subjectTypesSupported)).append("\n");
        sb.append("    idTokenSigningAlgValuesSupported: ")
            .append(this.toIndentedString(this.idTokenSigningAlgValuesSupported)).append("\n");
        sb.append("    registrationEndpoint: ").append(this.toIndentedString(this.registrationEndpoint)).append("\n");
        sb.append("    requestObjectSigningAlgValuesSupported: ")
            .append(this.toIndentedString(this.requestObjectSigningAlgValuesSupported)).append("\n");
        sb.append("    requestObjectEncryptionAlgValuesSupported: ")
            .append(this.toIndentedString(this.requestObjectEncryptionAlgValuesSupported)).append("\n");
        sb.append("    rcsResponseSigningAlgValuesSupported: ")
            .append(this.toIndentedString(this.rcsResponseSigningAlgValuesSupported)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
