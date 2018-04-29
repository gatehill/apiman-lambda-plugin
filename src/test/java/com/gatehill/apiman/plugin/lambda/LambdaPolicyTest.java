package com.gatehill.apiman.plugin.lambda;

import io.apiman.test.policies.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link LambdaPolicy}.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(LambdaPolicy.class)
public class LambdaPolicyTest extends ApimanPolicyTest {
    private static final String RESOURCE = "/some/resource";

    @Test
    @Configuration(classpathConfigFile = "standard-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testInvokeLambda() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, RESOURCE);
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());

        // no content should be returned
        assertEquals("Hello world!", response.body());
    }
}
