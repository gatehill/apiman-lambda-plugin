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

import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import com.gatehill.apiman.plugin.lambda.model.HttpExchange;
import com.gatehill.apiman.plugin.lambda.model.HttpRequest;
import com.gatehill.apiman.plugin.lambda.model.HttpResponse;
import com.gatehill.apiman.plugin.lambda.plumbing.AbstractWriteThroughStream;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.ApiResponse;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Invokes a Lambda function on the response, allowing it to be mutated
 * prior to transmission to the back end service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class LambdaResponsePolicy extends AbstractLambdaMessagePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaResponsePolicy.class);

    @Override
    protected void doApply(ApiRequest request, IPolicyContext context, LambdaPolicyConfig config, IPolicyChain<ApiRequest> chain) {
        context.setAttribute("request", new HttpRequest(request));
        chain.doApply(request);
    }

    @Override
    protected IReadWriteStream<ApiResponse> responseDataHandler(ApiResponse response, IPolicyContext context,
                                                                LambdaPolicyConfig config) {

        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        final IApimanBuffer responseBody = bufferFactory.createBuffer();

        return new AbstractWriteThroughStream<ApiResponse>() {
            @Override
            public ApiResponse getHead() {
                return response;
            }

            @Override
            protected void handleHead(ApiResponse head) {
                // no op
            }

            @Override
            public void write(IApimanBuffer chunk) {
                responseBody.append(chunk);
            }

            @Override
            public void end() {
                final CountDownLatch latch = new CountDownLatch(1);

                final HttpRequest httpRequest = context.getAttribute("request", null);
                final HttpResponse embeddedResponse = new HttpResponse(response, responseBody);
                final HttpExchange exchange = new HttpExchange(httpRequest, embeddedResponse);

                invokeLambda(config, HttpResponse.class, exchange).thenAccept(httpResponse -> {
                    try {
                        copyHeaderAndBody(bufferFactory, this, httpResponse, response);
                    } finally {
                        super.end();
                        latch.countDown();
                    }

                }).exceptionally(cause -> {
                    // TODO handle error
                    LOGGER.error("Error invoking lambda function: {} on response: {}",
                            config.getFunctionName(), httpRequest.getUrl(), cause);

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
}
