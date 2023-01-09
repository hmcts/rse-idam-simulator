package uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"PMD"})
public class JsonWebKeySet {

    @JsonProperty("keys")
    private List<JsonWebKey> keys = null;

    public JsonWebKeySet() {
    }

    public JsonWebKeySet keys(List<JsonWebKey> keys) {
        this.keys = keys;
        return this;
    }

    public JsonWebKeySet addKeysItem(JsonWebKey keysItem) {
        if (this.keys == null) {
            this.keys = new ArrayList<JsonWebKey>();
        }

        this.keys.add(keysItem);
        return this;
    }

    @ApiModelProperty("The value of the \"keys\" parameter is an array of JWK values.")
    public List<JsonWebKey> getKeys() {
        return this.keys;
    }

    public void setKeys(List<JsonWebKey> keys) {
        this.keys = keys;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            JsonWebKeySet jsonWebKeySet = (JsonWebKeySet) o;
            return Objects.equals(this.keys, jsonWebKeySet.keys);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.keys});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class JsonWebKeySet {\n");
        sb.append("    keys: ").append(this.toIndentedString(this.keys)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

