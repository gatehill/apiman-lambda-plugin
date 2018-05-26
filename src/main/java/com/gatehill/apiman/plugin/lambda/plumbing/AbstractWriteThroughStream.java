package com.gatehill.apiman.plugin.lambda.plumbing;

import io.apiman.gateway.engine.io.AbstractStream;
import io.apiman.gateway.engine.io.IApimanBuffer;

/**
 * @author Pete Cornish {@literal <outofcoffee@gmail.com>}
 */
public abstract class AbstractWriteThroughStream<H> extends AbstractStream<H> {
    private final H message;

    protected AbstractWriteThroughStream(H message) {
        this.message = message;
    }

    @Override
    public H getHead() {
        return message;
    }

    @Override
    protected void handleHead(H head) {
        // no op
    }

    public void writeThrough(IApimanBuffer chunk) {
        super.write(chunk);
    }
}
