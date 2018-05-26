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
import com.gatehill.apiman.plugin.lambda.model.HttpRequest;
import com.gatehill.apiman.plugin.lambda.plumbing.AbstractWriteThroughStream;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.util.QueryMap;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.io.IApimanBuffer;
import io.apiman.gateway.engine.io.IReadWriteStream;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * Invokes a Lambda function on the request, allowing it to be mutated
 * prior to transmission to the back end service.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class LambdaRequestPolicy extends AbstractLambdaMessagePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaRequestPolicy.class);

    @Override
    protected IReadWriteStream<ApiRequest> requestDataHandler(ApiRequest request, IPolicyContext context,
                                                              LambdaPolicyConfig config) {

        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        final IApimanBuffer requestBody = bufferFactory.createBuffer();

        return new AbstractWriteThroughStream<ApiRequest>(request) {
            @Override
            public void write(IApimanBuffer chunk) {
                requestBody.append(chunk);
            }

            @Override
            public void end() {
                final CountDownLatch latch = new CountDownLatch(1);

                final HttpRequest httpMessage = new HttpRequest(request, requestBody);
                invokeLambda(config, httpMessage, HttpRequest.class).thenAccept(httpRequest -> {
                    try {
                        request.setType(httpRequest.getHttpMethod());
                        request.setUrl(httpRequest.getUrl());

                        final QueryMap queryParams = new QueryMap();
                        queryParams.putAll(httpRequest.getQueryParams());
                        request.setQueryParams(queryParams);

                        copyHeaderAndBody(bufferFactory, this, httpRequest, request);

                    } finally {
                        super.end();
                        latch.countDown();
                    }

                }).exceptionally(cause -> {
                    // TODO better error handling
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
}
