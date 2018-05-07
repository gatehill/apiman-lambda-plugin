package com.gatehill.apiman.plugin.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.apiman.test.policies.*;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link LambdaRequestPolicy}.
 *
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
@SuppressWarnings("nls")
@TestingPolicy(LambdaRequestPolicy.class)
public class LambdaRequestPolicyTest extends ApimanPolicyTest {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    @Test
    @Configuration(classpathConfigFile = "request-mutator-config.json")
    @BackEndApi(EchoBackEndApi.class)
    public void testInvokeLambda() throws Throwable {
        final PolicyTestRequest request = PolicyTestRequest.build(PolicyTestRequestType.GET, "/some/resource");
        request.body("foo");

        final PolicyTestResponse response = send(request);
        assertEquals(200, response.code());
        assertNotNull(response.body());

        final Map echoBody = JSON_MAPPER.readValue(response.body(), Map.class);
        assertEquals("POST", echoBody.get("method"));

        // verify headers
        final Map headers = (Map) echoBody.get("headers");
        assertNotNull(headers);
        assertEquals("bar", headers.get("X-Custom-Header"));

        // the echo backend hashes the request body ("Hello World!") set by the lambda
        assertEquals("d3486ae9136e7856bc42212385ea797094475802", echoBody.get("bodySha1"));
    }
}
