package com.gatehill.apiman.plugin.lambda.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.io.IApimanBuffer;
import org.apache.commons.codec.binary.Base64;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class HttpMessage {
    private Map<String, String> headers = emptyMap();
    private String body;

    @JsonProperty("isBase64Encoded")
    private boolean isBase64Encoded = false;

    HttpMessage() {
    }

    HttpMessage(HeaderMap headers, IApimanBuffer buffer) {
        setHeaders(headers.toMap());
        if (nonNull(buffer) && buffer.length() > 0) {
            setBody(Base64.encodeBase64String(buffer.getBytes()));
            setIsBase64Encoded(true);
        } else {
            setIsBase64Encoded(false);
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

    public boolean getIsBase64Encoded() {
        return isBase64Encoded;
    }

    public void setIsBase64Encoded(boolean isBase64Encoded) {
        this.isBase64Encoded = isBase64Encoded;
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
