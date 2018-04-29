package com.gatehill.apiman.plugin.lambda.model;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class HttpResponse extends HttpMessage {
    private Integer statusCode;

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
