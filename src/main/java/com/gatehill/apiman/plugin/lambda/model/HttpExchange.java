package com.gatehill.apiman.plugin.lambda.model;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpExchange {
    private HttpRequest request;
    private HttpResponse response;

    public HttpExchange() {
    }

    public HttpExchange(HttpRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "HttpExchange{" +
                "request=" + request +
                ", response=" + response +
                '}';
    }
}
