package com.gatehill.apiman.plugin.lambda.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.gatehill.apiman.plugin.lambda.beans.LambdaPolicyConfig;
import org.apache.commons.lang.StringUtils;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public final class LambdaClientUtil {
    private LambdaClientUtil() {
    }

    public static AWSLambdaAsync build(LambdaPolicyConfig config) {
        final AWSLambdaAsyncClientBuilder standardBuilder = AWSLambdaAsyncClientBuilder.standard();
        if (StringUtils.isNotBlank(config.getAwsLambdaEndpoint())) {
            standardBuilder.setEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(config.getAwsLambdaEndpoint(), null));
        }
        return standardBuilder.build();
    }
}
