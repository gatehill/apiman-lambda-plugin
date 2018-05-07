package com.gatehill.apiman.plugin.lambda;

import io.apiman.test.policies.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link LambdaBackendPolicy}.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(LambdaBackendPolicy.class)
public class LambdaBackendPolicyTest extends ApimanPolicyTest {
    @Test
    @Configuration(classpathConfigFile = "backend-config.json")
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
