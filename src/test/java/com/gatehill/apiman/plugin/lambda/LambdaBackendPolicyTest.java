package com.gatehill.apiman.plugin.lambda;

import com.gatehill.apiman.plugin.lambda.util.LambdaTestUtil;
import io.apiman.test.policies.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.localstack.LocalStackContainer;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link LambdaBackendPolicy}.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(LambdaBackendPolicy.class)
public class LambdaBackendPolicyTest extends ApimanPolicyTest {
    @ClassRule
    public static LocalStackContainer localStack = LambdaTestUtil.buildLocalStackContainer();

    @BeforeClass
    public static void setUp() throws Exception {
        LambdaTestUtil.createFunction("policyBackend");
    }

    /**
     * Configuration using a function name defined statically in configuration.
     */
    @Test
    @Configuration(classpathConfigFile = "policyBackend-static-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testInvokeLambdaStaticName() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // the lambda returns this body
        assertEquals("Hello world!", response.body());
    }

    /**
     * Configuration using a function name derived from the first path component in the request.
     */
    @Test
    @Configuration(classpathConfigFile = "policyBackend-wildcard-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testInvokeLambdaWildcardName() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/policyBackend/some/resource");
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // the lambda returns this body
        assertEquals("Hello world!", response.body());
    }
}
