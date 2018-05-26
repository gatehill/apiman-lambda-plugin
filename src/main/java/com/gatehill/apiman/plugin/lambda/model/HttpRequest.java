package com.gatehill.apiman.plugin.lambda.model;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.util.Collections;
import java.util.Map;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpRequest extends HttpMessage {
    private String url;
    private String httpMethod;
    private Map<String, String> queryParams = Collections.emptyMap();

    public HttpRequest() {
    }

    public HttpRequest(ApiRequest request, IApimanBuffer requestBuffer) {
        super(request.getHeaders(), requestBuffer);
        setUrl(request.getUrl());
        setHttpMethod(request.getType());
        setQueryParams(request.getQueryParams().toMap());
    }

    public HttpRequest(ApiRequest request) {
        this(request, null);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
                "url='" + url + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryParams=" + queryParams +
                "} " + super.toString();
    }
}
