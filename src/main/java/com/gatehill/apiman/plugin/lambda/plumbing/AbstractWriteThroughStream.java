package com.gatehill.apiman.plugin.lambda.plumbing;

import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;

/**
 * @author pete
 */
public abstract class AbstractWriteThroughStream<H> extends AbstractStream<H> {
    public void writeThrough(IApimanBuffer chunk) {
        super.write(chunk);
    }
}
