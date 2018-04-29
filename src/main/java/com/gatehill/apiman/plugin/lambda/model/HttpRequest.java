package com.gatehill.apiman.plugin.lambda.model;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.util.Collections;
import java.util.Map;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpRequest extends HttpMessage {
    private String httpMethod;
    private Map<String, String> queryParams = Collections.emptyMap();

    public HttpRequest(ApiRequest request, IApimanBuffer requestBuffer) {
        setHttpMethod(request.getType());
        setQueryParams(request.getQueryParams().toMap());
        setHeaders(request.getHeaders().toMap());
        setBody(requestBuffer.toString());
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "httpMethod='" + httpMethod + '\'' +
                ", queryParams=" + queryParams +
                "} " + super.toString();
    }
}
