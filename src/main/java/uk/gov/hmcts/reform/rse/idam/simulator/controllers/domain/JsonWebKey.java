package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    @JsonProperty("x5c")
    private List<String> x5c = null;
    @JsonProperty("x5t")
    private String x5t = null;
    @JsonProperty("crv")
    private String crv = null;
    @JsonProperty("d")
    private String d = null;
    @JsonProperty("dp")
    private String dp = null;
    @JsonProperty("dq")
    private String dq = null;
    @JsonProperty("k")
    private String k = null;
    @JsonProperty("p")
    private String p = null;
    @JsonProperty("q")
    private String q = null;
    @JsonProperty("qi")
    private String qi = null;
    @JsonProperty("x")
    private String x = null;
    @JsonProperty("y")
    private String y = null;

    public JsonWebKey() {
    }

    public JsonWebKey alg(String alg) {
        this.alg = alg;
        return this;
    }

    @ApiModelProperty("Algorithm intended for use with the key")
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

    @ApiModelProperty("The cryptographic algorithm family used with the key, such as \"RSA\" or \"EC\"")
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

    @ApiModelProperty("The intended use of the public key, such as \"sig\" (signature), \"enc\" (encryption)")
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

    @ApiModelProperty("Unique identifier for the key. It is used to match a specific key.")
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

    @ApiModelProperty("The modulus for a standard pem")
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

    @ApiModelProperty("The exponent for a standard pem")
    public String getE() {
        return this.e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public JsonWebKey x5c(List<String> x5c) {
        this.x5c = x5c;
        return this;
    }

    public JsonWebKey addX5cItem(String x5cItem) {
        if (this.x5c == null) {
            this.x5c = new ArrayList<String>();
        }

        this.x5c.add(x5cItem);
        return this;
    }

    @ApiModelProperty("The x.509 certificate chain")
    public List<String> getX5c() {
        return this.x5c;
    }

    public void setX5c(List<String> x5c) {
        this.x5c = x5c;
    }

    public JsonWebKey x5t(String x5t) {
        this.x5t = x5t;
        return this;
    }

    @ApiModelProperty("The thumbprint of the x.509 cert (SHA-1 thumbprint)")
    public String getX5t() {
        return this.x5t;
    }

    public void setX5t(String x5t) {
        this.x5t = x5t;
    }

    public JsonWebKey crv(String crv) {
        this.crv = crv;
        return this;
    }

    @ApiModelProperty("The cryptographic curve used with the key (one of P-256, P-384, P-521)")
    public String getCrv() {
        return this.crv;
    }

    public void setCrv(String crv) {
        this.crv = crv;
    }

    public JsonWebKey d(String d) {
        this.d = d;
        return this;
    }

    @ApiModelProperty("The Elliptic Curve private key value or the private exponent value for the RSA private key")
    public String getD() {
        return this.d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public JsonWebKey dp(String dp) {
        this.dp = dp;
        return this;
    }

    @ApiModelProperty("For an RSA private key, contains the Chinese Remainder Theorem (CRT) exponent of the first factor")
    public String getDp() {
        return this.dp;
    }

    public void setDp(String dp) {
        this.dp = dp;
    }

    public JsonWebKey dq(String dq) {
        this.dq = dq;
        return this;
    }

    @ApiModelProperty("For an RSA private key, contains the CRT exponent of the second factor")
    public String getDq() {
        return this.dq;
    }

    public void setDq(String dq) {
        this.dq = dq;
    }

    public JsonWebKey k(String k) {
        this.k = k;
        return this;
    }

    @ApiModelProperty("Contains the value of the symmetric (or other single-valued) key")
    public String getK() {
        return this.k;
    }

    public void setK(String k) {
        this.k = k;
    }

    public JsonWebKey p(String p) {
        this.p = p;
        return this;
    }

    @ApiModelProperty("For an RSA private key, contains the first prime factor.")
    public String getP() {
        return this.p;
    }

    public void setP(String p) {
        this.p = p;
    }

    public JsonWebKey q(String q) {
        this.q = q;
        return this;
    }

    @ApiModelProperty("For an RSA private key, contains the second prime factor.")
    public String getQ() {
        return this.q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public JsonWebKey qi(String qi) {
        this.qi = qi;
        return this;
    }

    @ApiModelProperty("For an RSA private key, contains the CRT coefficient of the second factor")
    public String getQi() {
        return this.qi;
    }

    public void setQi(String qi) {
        this.qi = qi;
    }

    public JsonWebKey x(String x) {
        this.x = x;
        return this;
    }

    @ApiModelProperty("The x coordinate for the Elliptic Curve point")
    public String getX() {
        return this.x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public JsonWebKey y(String y) {
        this.y = y;
        return this;
    }

    @ApiModelProperty("The y coordinate for the Elliptic Curve point")
    public String getY() {
        return this.y;
    }

    public void setY(String y) {
        this.y = y;
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
            ) && Objects.equals(this.e, jsonWebKey.e) && Objects.equals(
                this.x5c,
                jsonWebKey.x5c
            ) && Objects.equals(this.x5t, jsonWebKey.x5t) && Objects.equals(this.crv, jsonWebKey.crv) && Objects.equals(
                this.d,
                jsonWebKey.d
            ) && Objects.equals(this.dp, jsonWebKey.dp) && Objects.equals(
                this.dq,
                jsonWebKey.dq
            ) && Objects.equals(this.k, jsonWebKey.k) && Objects.equals(this.p, jsonWebKey.p) && Objects.equals(
                this.q,
                jsonWebKey.q
            ) && Objects.equals(this.qi, jsonWebKey.qi) && Objects.equals(
                this.x,
                jsonWebKey.x
            ) && Objects.equals(this.y, jsonWebKey.y);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.alg, this.kty, this.use, this.kid, this.n, this.e, this.x5c, this.x5t, this.crv, this.d, this.dp, this.dq, this.k, this.p, this.q, this.qi, this.x, this.y});
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
        sb.append("    x5c: ").append(this.toIndentedString(this.x5c)).append("\n");
        sb.append("    x5t: ").append(this.toIndentedString(this.x5t)).append("\n");
        sb.append("    crv: ").append(this.toIndentedString(this.crv)).append("\n");
        sb.append("    d: ").append(this.toIndentedString(this.d)).append("\n");
        sb.append("    dp: ").append(this.toIndentedString(this.dp)).append("\n");
        sb.append("    dq: ").append(this.toIndentedString(this.dq)).append("\n");
        sb.append("    k: ").append(this.toIndentedString(this.k)).append("\n");
        sb.append("    p: ").append(this.toIndentedString(this.p)).append("\n");
        sb.append("    q: ").append(this.toIndentedString(this.q)).append("\n");
        sb.append("    qi: ").append(this.toIndentedString(this.qi)).append("\n");
        sb.append("    x: ").append(this.toIndentedString(this.x)).append("\n");
        sb.append("    y: ").append(this.toIndentedString(this.y)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

