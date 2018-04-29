package com.gatehill.apiman.plugin.lambda.model;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class HttpMessage {
    private Map<String, String> headers = emptyMap();
    private String body;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "HttpMessage{" +
                "headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
