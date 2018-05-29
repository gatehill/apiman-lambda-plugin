package com.gatehill.apiman.plugin.lambda.model;

import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.util.Collections;
import java.util.Map;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpRequest extends HttpMessage {
    private String path;
    private String httpMethod;
    private Map<String, String> queryStringParameters = Collections.emptyMap();

    public HttpRequest() {
    }

    public HttpRequest(ApiRequest request, IApimanBuffer requestBuffer) {
        super(request.getHeaders(), requestBuffer);
        setPath(request.getDestination());
        setHttpMethod(request.getType());
        setQueryStringParameters(request.getQueryParams().toMap());
    }

    public HttpRequest(ApiRequest request) {
        this(request, null);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getQueryStringParameters() {
        return queryStringParameters;
    }

    public void setQueryStringParameters(Map<String, String> queryStringParameters) {
        this.queryStringParameters = queryStringParameters;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "path='" + path + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryStringParameters=" + queryStringParameters +
                "} " + super.toString();
    }
}
