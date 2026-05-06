package com.yas.cart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    @Test
    void handleTypedFallback_rethrowsException() {
        TestHandler handler = new TestHandler();
        RuntimeException failure = new RuntimeException("boom");

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> handler.callTyped(failure));

        assertEquals("boom", thrown.getMessage());
    }

    @Test
    void handleBodilessFallback_rethrowsException() {
        TestHandler handler = new TestHandler();
        IllegalStateException failure = new IllegalStateException("bad");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
            () -> handler.callBodiless(failure));

        assertEquals("bad", thrown.getMessage());
    }

    private static final class TestHandler extends AbstractCircuitBreakFallbackHandler {
        private void callBodiless(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        private Object callTyped(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }
}
