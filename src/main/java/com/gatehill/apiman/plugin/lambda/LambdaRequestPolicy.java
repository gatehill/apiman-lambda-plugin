/*
 * Copyright 2018 Pete Cornish
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gatehill.apiman.plugin.lambda;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import com.gatehill.apiman.plugin.lambda.model.HttpRequest;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.beans.util.HeaderMap;
import io.apiman.gateway.engine.beans.util.QueryMap;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policies.AbstractMappedDataPolicy;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * Invokes a Lambda function on the request, allowing it to be mutated
 * prior to transmission to the back end service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class LambdaRequestPolicy extends AbstractMappedDataPolicy<LambdaPolicyConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaRequestPolicy.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Override
    protected Class<LambdaPolicyConfig> getConfigurationClass() {
        return LambdaPolicyConfig.class;
    }

    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request, IPolicyContext context,
                                                              LambdaPolicyConfig config) {

        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        final IApimanBuffer requestBody = bufferFactory.createBuffer();

        return new AbstractStream<ApiRequest>() {
            @Override
            public ApiRequest getHead() {
                return request;
            }

            @Override
            protected void handleHead(ApiRequest head) {
                // no op
            }

            @Override
            public void write(IApimanBuffer chunk) {
                requestBody.append(chunk);
            }

            @Override
            public void end() {
                final CountDownLatch latch = new CountDownLatch(1);

                invokeLambda(config, request, requestBody).thenAccept(httpRequest -> {
                    try {
                        request.setType(httpRequest.getHttpMethod());
                        request.setUrl(httpRequest.getUrl());

                        final QueryMap queryParams = new QueryMap();
                        queryParams.putAll(httpRequest.getQueryParams());
                        request.setQueryParams(queryParams);

                        final HeaderMap headers = new HeaderMap();
                        headers.putAll(httpRequest.getHeaders());
                        request.setHeaders(headers);

                        super.write(bufferFactory.createBuffer(httpRequest.getBody()));

                    } finally {
                        super.end();
                        latch.countDown();
                    }

                }).exceptionally(cause -> {
                    // TODO handle error
                    LOGGER.error("Error invoking lambda function: {} on request: {}",
                            config.getFunctionName(), request.getUrl(), cause);

                    super.end();
                    latch.countDown();
                    return null;
                });

                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                }
            }
        };
    }

    private CompletableFuture<HttpRequest> invokeLambda(LambdaPolicyConfig config,
                                                        ApiRequest request, IApimanBuffer requestBuffer) {

        LOGGER.debug("Invoking lambda function: {}", config.getFunctionName());
        final CompletableFuture<HttpRequest> future = new CompletableFuture<>();

        final InvokeRequest invokeRequest = new InvokeRequest();
        invokeRequest.setFunctionName(config.getFunctionName());
        invokeRequest.setInvocationType(InvocationType.RequestResponse);

        try {
            invokeRequest.setPayload(JSON_MAPPER.writeValueAsString(new HttpRequest(request, requestBuffer)));
        } catch (JsonProcessingException e) {
            future.completeExceptionally(e);
            return future;
        }

        final AsyncHandler<InvokeRequest, InvokeResult> asyncHandler = new AsyncHandler<InvokeRequest, InvokeResult>() {
            @Override
            public void onSuccess(InvokeRequest request, InvokeResult invokeResult) {
                try {
                    LOGGER.info("Lambda function: {} returned successfully", config.getFunctionName());
                    final HttpRequest returnedRequest = JSON_MAPPER.readValue(
                            new ByteBufferBackedInputStream(invokeResult.getPayload()), HttpRequest.class);

                    future.complete(returnedRequest);

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

        AWSLambdaAsyncClientBuilder.defaultClient().invokeAsync(invokeRequest, asyncHandler);
        return future;
    }

    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response, IPolicyContext context,
                                                                LambdaPolicyConfig config) {
        return null;
    }
}
