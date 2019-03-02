package com.gatehill.apiman.plugin.lambda.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for Lambda Backend Policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class LambdaBackendPolicyConfig extends LambdaPolicyConfig {
    @JsonProperty
    private Boolean wildcard;

    public Boolean getWildcard() {
        return wildcard;
    }

    public void setWildcard(Boolean wildcard) {
        this.wildcard = wildcard;
    }
}
