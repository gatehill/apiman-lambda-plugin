package com.gatehill.apiman.plugin.lambda.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration object for the Lambda Policy.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class LambdaPolicyConfig {
    @JsonProperty
    private String functionName;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }
}
