package com.gatehill.apiman.plugin.lambda.model;

import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.io.IApimanBuffer;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpResponse extends HttpMessage {
    private Integer statusCode;

    public HttpResponse() {
    }

    public HttpResponse(ApiResponse response, IApimanBuffer responseBuffer) {
        super(response.getHeaders(), responseBuffer);
        setStatusCode(response.getCode());
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                "} " + super.toString();
    }
}
