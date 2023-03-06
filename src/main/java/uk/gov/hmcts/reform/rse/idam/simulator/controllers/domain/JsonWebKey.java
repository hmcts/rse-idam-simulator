package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@SuppressWarnings({"PMD", "checkstyle:MemberName", "checkstyle:MethodName"})
public class JsonWebKey {

    @JsonProperty("alg")
    private String alg = null;
    @JsonProperty("kty")
    private String kty = null;
    @JsonProperty("use")
    private String use = null;
    @JsonProperty("kid")
    private String kid = null;
    @JsonProperty("n")
    private String n = null;
    @JsonProperty("e")
    private String e = null;

    public JsonWebKey() {
    }

    public JsonWebKey alg(String alg) {
        this.alg = alg;
        return this;
    }


    public String getAlg() {
        return this.alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public JsonWebKey kty(String kty) {
        this.kty = kty;
        return this;
    }


    public String getKty() {
        return this.kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public JsonWebKey use(String use) {
        this.use = use;
        return this;
    }


    public String getUse() {
        return this.use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public JsonWebKey kid(String kid) {
        this.kid = kid;
        return this;
    }


    public String getKid() {
        return this.kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public JsonWebKey n(String n) {
        this.n = n;
        return this;
    }


    public String getN() {
        return this.n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public JsonWebKey e(String e) {
        this.e = e;
        return this;
    }


    public String getE() {
        return this.e;
    }

    public void setE(String e) {
        this.e = e;
    }


    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            JsonWebKey jsonWebKey = (JsonWebKey) o;
            return Objects.equals(this.alg, jsonWebKey.alg) && Objects.equals(
                this.kty,
                jsonWebKey.kty
            ) && Objects.equals(this.use, jsonWebKey.use) && Objects.equals(this.kid, jsonWebKey.kid) && Objects.equals(
                this.n,
                jsonWebKey.n
            ) && Objects.equals(this.e, jsonWebKey.e);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.alg, this.kty, this.use, this.kid, this.n, this.e});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JsonWebKey {\n");
        sb.append("    alg: ").append(this.toIndentedString(this.alg)).append("\n");
        sb.append("    kty: ").append(this.toIndentedString(this.kty)).append("\n");
        sb.append("    use: ").append(this.toIndentedString(this.use)).append("\n");
        sb.append("    kid: ").append(this.toIndentedString(this.kid)).append("\n");
        sb.append("    n: ").append(this.toIndentedString(this.n)).append("\n");
        sb.append("    e: ").append(this.toIndentedString(this.e)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

