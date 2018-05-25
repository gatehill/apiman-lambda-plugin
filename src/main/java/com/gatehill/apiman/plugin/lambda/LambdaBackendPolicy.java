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
import com.gatehill.apiman.plugin.lambda.plumbing.LambdaConnectorInterceptor;
import io.apiman.gateway.engine.beans.ApiRequest;
import io.apiman.gateway.engine.components.IBufferFactoryComponent;
import io.apiman.gateway.engine.policies.AbstractMappedPolicy;
import io.apiman.gateway.engine.policy.IPolicyChain;
import io.apiman.gateway.engine.policy.IPolicyContext;

/**
 * Invokes a Lambda function instead of the configured backend API.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
public class LambdaBackendPolicy extends AbstractMappedPolicy<LambdaPolicyConfig> {
    @Override
    protected Class<LambdaPolicyConfig> getConfigurationClass() {
        return LambdaPolicyConfig.class;
    }

    @Override
    protected void doApply(final ApiRequest request, final IPolicyContext context,
                           final LambdaPolicyConfig config, final IPolicyChain<ApiRequest> chain) {

        final IBufferFactoryComponent bufferFactory = context.getComponent(IBufferFactoryComponent.class);
        context.setConnectorInterceptor(() -> new LambdaConnectorInterceptor(config, bufferFactory));
        chain.doApply(request);
    }
}
