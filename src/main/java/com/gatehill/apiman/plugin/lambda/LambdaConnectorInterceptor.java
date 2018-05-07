package com.gatehill.apiman.plugin.lambda;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import com.gatehill.apiman.plugin.lambda.model.HttpRequest;
import io.apiman.gateway.engine.IApiConnection;
import io.apiman.gateway.engine.IApiConnectionResponse;
import io.apiman.gateway.engine.IApiConnector;
import io.apiman.gateway.engine.async.AsyncResultImpl;
import io.apiman.gateway.engine.async.IAsyncResultHandler;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.policy.IConnectorInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import static java.util.Objects.nonNull;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public class LambdaConnectorInterceptor implements IConnectorInterceptor, IApiConnector, IApiConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaConnectorInterceptor.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private final LambdaPolicyConfig config;
    private final IBufferFactoryComponent bufferFactory;
    private ApiRequest request;
    private IAsyncResultHandler<IApiConnectionResponse> responseHandler;
    private IApimanBuffer requestBuffer;
    private boolean finished = false;
    private boolean connected = false;
    private Future<InvokeResult> invokeFuture;

    LambdaConnectorInterceptor(LambdaPolicyConfig config, IBufferFactoryComponent bufferFactory) {
        this.config = config;
        this.bufferFactory = bufferFactory;
    }

    @Override
    public IApiConnector createConnector() {
        return this;
    }

    @Override
    public IApiConnection connect(ApiRequest request, IAsyncResultHandler<IApiConnectionResponse> handler) {
        LOGGER.debug("Received connection request for function: {}", config.getFunctionName());
        this.request = request;
        this.responseHandler = handler;
        this.connected = true;
        requestBuffer = bufferFactory.createBuffer();

        return this;
    }

    @Override
    public void abort(Throwable t) {
        if (!finished) {
            LOGGER.debug("Aborting lambda connection for function: {}", config.getFunctionName());
            finished = true;
            connected = false;

            if (nonNull(invokeFuture) && !invokeFuture.isCancelled()) {
                invokeFuture.cancel(true);
            }
        }
    }

    @Override
    public void write(IApimanBuffer chunk) {
        requestBuffer.append(chunk);
    }

    /**
     * Called when the request has been submitted.
     */
    @Override
    public void end() {
        LOGGER.debug("Invoking lambda function: {}", config.getFunctionName());

        final InvokeRequest invokeRequest = new InvokeRequest();
        invokeRequest.setFunctionName(config.getFunctionName());
        invokeRequest.setInvocationType(InvocationType.RequestResponse);

        try {
            invokeRequest.setPayload(JSON_MAPPER.writeValueAsString(new HttpRequest(request, requestBuffer)));
        } catch (JsonProcessingException e) {
            responseHandler.handle(AsyncResultImpl.create(e));
            return;
        }

        final CountDownLatch latch = new CountDownLatch(1);

        final AsyncHandler<InvokeRequest, InvokeResult> asyncHandler = new AsyncHandler<InvokeRequest, InvokeResult>() {
            @Override
            public void onSuccess(InvokeRequest request, InvokeResult invokeResult) {
                try {
                    if (finished) {
                        // we may already have aborted
                        return;
                    }
                    LOGGER.info("Lambda function: {} returned successfully", config.getFunctionName());
                    final LambdaConnectionResponse connectionResponse = new LambdaConnectionResponse(
                            config, bufferFactory, LambdaConnectorInterceptor.this, invokeResult.getPayload());

                    responseHandler.handle(AsyncResultImpl.create(connectionResponse));

                } catch (IOException e) {
                    responseHandler.handle(AsyncResultImpl.create(e));
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(Exception e) {
                try {
                    if (finished) {
                        // we may already have aborted
                        return;
                    }
                    LOGGER.error("Lambda function: {} returned an error", config.getFunctionName(), e);
                    responseHandler.handle(AsyncResultImpl.create(e));

                } finally {
                    latch.countDown();
                }
            }
        };

        invokeFuture = AWSLambdaAsyncClientBuilder.defaultClient().invokeAsync(invokeRequest, asyncHandler);
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
    }

    void setFinished(boolean finished) {
        this.finished = finished;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }
}
