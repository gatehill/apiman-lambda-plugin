package com.gatehill.apiman.plugin.lambda.model;

import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.io.IApimanBuffer;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class HttpMessage {
    private Map<String, String> headers = emptyMap();
    private String body;
    private boolean isBase64Encoded = false;

    HttpMessage() {
    }

    HttpMessage(HeaderMap headers, IApimanBuffer buffer) {
        setHeaders(headers.toMap());
        if (nonNull(buffer)) {
            setBody(buffer.toString());
        }
    }

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

    public boolean isBase64Encoded() {
        return isBase64Encoded;
    }

    public void setBase64Encoded(boolean base64Encoded) {
        isBase64Encoded = base64Encoded;
    }

    @Override
    public String toString() {
        return "HttpMessage{" +
                "headers=" + headers +
                ", body='" + body + '\'' +
                ", isBase64Encoded=" + isBase64Encoded +
                '}';
    }
}
