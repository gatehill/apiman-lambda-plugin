package com.gatehill.apiman.plugin.lambda;

import com.gatehill.apiman.plugin.lambda.util.LambdaTestUtil;
import io.apiman.test.policies.*;
import org.junit.Before;
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

    @Before
    public void setUp() throws Exception {
        LambdaTestUtil.createFunction("policyBackend");
    }

    @Test
    @Configuration(classpathConfigFile = "policyBackend-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testInvokeLambda() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // the lambda returns this body
        assertEquals("Hello world!", response.body());
    }
}
