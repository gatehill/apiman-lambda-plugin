package com.gatehill.apiman.plugin.lambda.util;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.gatehill.apiman.plugin.lambda.LambdaBackendPolicyTest;
import org.apache.commons.io.FileUtils;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public final class LambdaTestUtil {
    private static final int LOCAL_LAMBDA_PORT = 4574;

    private LambdaTestUtil() {
    }

    public static LocalStackContainer buildLocalStackContainer() {
        final LocalStackContainer container = new LocalStackContainer() {{
            setPortBindings(Lists.newArrayList(LOCAL_LAMBDA_PORT + ":" + LOCAL_LAMBDA_PORT));
        }};

        return container
                .withServices(LocalStackContainer.Service.LAMBDA)
                .withEnv("LAMBDA_EXECUTOR", "docker");
    }

    public static void createFunction(String functionName) throws IOException, URISyntaxException {
        final File codeFile = new File(LambdaBackendPolicyTest.class.getResource("/" + functionName + ".zip").toURI());
        final byte[] zipFile = FileUtils.readFileToByteArray(codeFile);

        final CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest()
                .withFunctionName(functionName)
                .withHandler(functionName + ".handler")
                .withRole("dummy-role")
                .withRuntime("nodejs6.10")
                .withCode(new FunctionCode().withZipFile(ByteBuffer.wrap(zipFile)));

        AWSLambdaClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://localhost:" + LOCAL_LAMBDA_PORT, null))
                .build().createFunction(createFunctionRequest);
    }
}
