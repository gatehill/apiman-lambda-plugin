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

import com.gatehill.apiman.plugin.lambda.beans.LambdaBackendPolicyConfig;
import com.gatehill.apiman.plugin.lambda.plumbing.LambdaConnectorInterceptor;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.beans.PolicyFailure;
import io.apiman.gateway.engine.beans.PolicyFailureType;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Invokes a Lambda function instead of the configured backend API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class LambdaBackendPolicy extends AbstractMappedPolicy<LambdaBackendPolicyConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaBackendPolicy.class);

    @Override
    protected Class<LambdaBackendPolicyConfig> getConfigurationClass() {
        return LambdaBackendPolicyConfig.class;
    }

    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context,
                           final LambdaBackendPolicyConfig config, final IPolicyChain<ApiRequest> chain) {

        final String functionName;
        if (Boolean.TRUE.equals(config.getWildcard())) {
            try {
                // example: /function-name/foo/bar
                final String[] pathElements = request.getDestination().split("/");

                if (pathElements.length <= 1) {
                    throw new IllegalStateException("No function name found in request path");
                }

                functionName = pathElements[1];

                // strip function name from downstream request path
                request.setDestination("/" + String.join("/", Arrays.copyOfRange(pathElements, 2, pathElements.length)));

            } catch (Exception e) {
                LOGGER.error("Error determining function name from request path: {}", request.getDestination(), e);
                chain.doFailure(new PolicyFailure(PolicyFailureType.Other, 400, "Unable to determine function name from request path"));
                return;
            }
        } else {
            functionName = config.getFunctionName();
        }

        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        context.setConnectorInterceptor(() -> new LambdaConnectorInterceptor(functionName, config, bufferFactory));
        chain.doApply(request);
    }
}
