/*
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.netty.contexts;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

/**
 * @author Nitesh Kant
 */
public class ThreadLocalCorrelatorTest {

    @Test
    public void testSetAndGet() throws Exception {
        NoOpChannelHandlerContext ctx = new NoOpChannelHandlerContext();
        final ThreadLocalRequestCorrelator correlator = new ThreadLocalRequestCorrelator();
        final ContextKeySupplier supplier = new MapBackedKeySupplier(Collections.<String, String>emptyMap());
        final ContextsContainer container = new ContextsContainerImpl(supplier);
        final String requestId = "Blah";

        correlator.onNewServerRequest(requestId, container);

        assertRequestIdAndContainer(ctx, correlator, container, requestId);
    }

    @Test
    public void testCallableClosure() throws Exception {
        final NoOpChannelHandlerContext ctx = new NoOpChannelHandlerContext();
        final ThreadLocalRequestCorrelator correlator = new ThreadLocalRequestCorrelator();
        final ContextKeySupplier supplier = new MapBackedKeySupplier(Collections.<String, String>emptyMap());
        final ContextsContainer container = new ContextsContainerImpl(supplier);
        final String requestId = "Blah";

        correlator.onNewServerRequest(requestId, container);

        Callable<Void> closure = correlator.makeClosure(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Assert.assertSame("Invalid request Id inside callable.", requestId,
                                  correlator.getRequestIdForClientRequest(ctx));
                Assert.assertSame("Invalid context container callable.", container,
                                  correlator.getContextForClientRequest(requestId, ctx));
                return null;
            }
        });

        Executors.newFixedThreadPool(1).submit(closure).get(); // Waits for assertion.

        assertRequestIdAndContainer(ctx, correlator, container, requestId);
    }

    @Test
    public void testRunnableClosure() throws Exception {
        final NoOpChannelHandlerContext ctx = new NoOpChannelHandlerContext();
        final ThreadLocalRequestCorrelator correlator = new ThreadLocalRequestCorrelator();
        final ContextKeySupplier supplier = new MapBackedKeySupplier(Collections.<String, String>emptyMap());
        final ContextsContainer container = new ContextsContainerImpl(supplier);
        final String requestId = "Blah";

        correlator.onNewServerRequest(requestId, container);

        Runnable closure = correlator.makeClosure(new Runnable() {

            @Override
            public void run() {
                Assert.assertSame("Invalid request Id inside callable.", requestId,
                                  correlator.getRequestIdForClientRequest(ctx));
                Assert.assertSame("Invalid context container callable.", container,
                                  correlator.getContextForClientRequest(requestId, ctx));
            }
        });

        Executors.newFixedThreadPool(1).submit(closure).get(); // Waits for assertion.

        assertRequestIdAndContainer(ctx, correlator, container, requestId);
    }

    private static void assertRequestIdAndContainer(NoOpChannelHandlerContext ctx,
                                                    ThreadLocalRequestCorrelator correlator,
                                                    ContextsContainer container, String requestId) {
        Assert.assertSame("Unexpected request id.", requestId, correlator.getRequestIdForClientRequest(ctx));
        Assert.assertSame("Unexpected context container.", container,
                          correlator.getContextForClientRequest(requestId, ctx));
    }
}
