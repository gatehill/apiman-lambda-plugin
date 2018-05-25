package com.gatehill.apiman.plugin.lambda;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import com.gatehill.apiman.plugin.lambda.model.HttpMessage;
import com.gatehill.apiman.plugin.lambda.plumbing.AbstractWriteThroughStream;
import com.gatehill.apiman.plugin.lambda.util.LambdaClientUtil;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.IApiObject;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
abstract class AbstractLambdaMessagePolicy extends AbstractMappedDataPolicy<LambdaPolicyConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLambdaMessagePolicy.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Override
    protected Class<LambdaPolicyConfig> getConfigurationClass() {
        return LambdaPolicyConfig.class;
    }

    <R> CompletableFuture<R> invokeLambda(LambdaPolicyConfig config, Class<R> resultClass, Object httpMessage) {
        LOGGER.debug("Invoking lambda function: {}", config.getFunctionName());
        final CompletableFuture<R> future = new CompletableFuture<>();

        final InvokeRequest invokeResponse = new InvokeRequest();
        invokeResponse.setFunctionName(config.getFunctionName());
        invokeResponse.setInvocationType(InvocationType.RequestResponse);

        try {
            invokeResponse.setPayload(JSON_MAPPER.writeValueAsString(httpMessage));
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
            return future;
        }

        final AsyncHandler<InvokeRequest, InvokeResult> asyncHandler = new AsyncHandler<InvokeRequest, InvokeResult>() {
            @Override
            public void onSuccess(InvokeRequest request, InvokeResult invokeResult) {
                try {
                    LOGGER.info("Lambda function: {} returned successfully", config.getFunctionName());
                    final R returnedResponse = JSON_MAPPER.readValue(
                            new ByteBufferBackedInputStream(invokeResult.getPayload()), resultClass);

                    future.complete(returnedResponse);

                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onError(Exception e) {
                LOGGER.error("Lambda function: {} returned an error", config.getFunctionName(), e);
                future.completeExceptionally(e);
            }
        };

        final AWSLambdaAsync lambdaClient = LambdaClientUtil.build(config);
        lambdaClient.invokeAsync(invokeResponse, asyncHandler);
        return future;
    }

    void copyHeaderAndBody(IBufferFactoryComponent bufferFactory, AbstractWriteThroughStream stream,
                           HttpMessage httpMessage, IApiObject message) {

        final HeaderMap headers = new HeaderMap();
        headers.putAll(httpMessage.getHeaders());
        message.setHeaders(headers);

        stream.writeThrough(bufferFactory.createBuffer(httpMessage.getBody()));
    }

    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request, IPolicyContext context,
                                                              LambdaPolicyConfig config) {
        return null; // default to no-op
    }

    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response, IPolicyContext context,
                                                                LambdaPolicyConfig config) {
        return null; // default to no-op
    }
}
