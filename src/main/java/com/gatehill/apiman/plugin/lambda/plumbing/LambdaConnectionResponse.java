package com.gatehill.apiman.plugin.lambda.plumbing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import com.gatehill.apiman.plugin.lambda.model.HttpResponse;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.async.IAsyncHandler;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
class LambdaConnectionResponse implements IApiConnectionResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaConnectionResponse.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final LambdaPolicyConfig config;
    private final IBufferFactoryComponent bufferFactory;
    private final LambdaConnectorInterceptor connectorInterceptor;
    private final HttpResponse httpResponse;
    private IAsyncHandler<IApimanBuffer> bodyHandler;
    private IAsyncHandler<Void> endHandler;

    LambdaConnectionResponse(LambdaPolicyConfig config, IBufferFactoryComponent bufferFactory,
                             LambdaConnectorInterceptor connectorInterceptor, ByteBuffer responsePayload) throws IOException {

        this.config = config;
        this.bufferFactory = bufferFactory;
        this.connectorInterceptor = connectorInterceptor;
        this.httpResponse = JSON_MAPPER.readValue(new ByteBufferBackedInputStream(responsePayload), HttpResponse.class);
    }

    @Override
    public void transmit() {
        LOGGER.debug("Lambda function: {} transmitting", config.getFunctionName());
        bodyHandler.handle(bufferFactory.createBuffer(httpResponse.getBody()));
        endHandler.handle(null);

        connectorInterceptor.setConnected(false);
        connectorInterceptor.setFinished(true);
    }

    @Override
    public void abort(Throwable t) {
        if (!connectorInterceptor.isFinished()) {
            LOGGER.debug("Aborting lambda response for function: {}", config.getFunctionName());
            connectorInterceptor.setConnected(false);
            connectorInterceptor.setFinished(true);
        }
    }

    @Override
    public void bodyHandler(IAsyncHandler<IApimanBuffer> bodyHandler) {
        this.bodyHandler = bodyHandler;
    }

    @Override
    public void endHandler(IAsyncHandler<Void> endHandler) {
        this.endHandler = endHandler;
    }

    @Override
    public ApiResponse getHead() {
        final ApiResponse response = new ApiResponse();

        final int statusCode = ofNullable(httpResponse.getStatusCode()).orElse(200);
        response.setCode(statusCode);
        response.setMessage(EnglishReasonPhraseCatalog.INSTANCE.getReason(statusCode, null));
        response.getHeaders().putAll(ofNullable(httpResponse.getHeaders()).orElse(emptyMap()));

        return response;
    }

    @Override
    public boolean isFinished() {
        return connectorInterceptor.isFinished();
    }
}
