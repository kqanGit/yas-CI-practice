package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

  // Concrete implementation for testing
  private TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

  @Test
  void handleBodilessFallback_shouldThrowOriginalException() throws Throwable {
    IOException originalException = new IOException("Original error");

    assertThrows(IOException.class, () -> handler.testHandleBodilessFallback(originalException));
  }

  @Test
  void handleTypedFallback_shouldThrowOriginalException_andReturnNull() throws Throwable {
    IOException originalException = new IOException("Original error");

    // The method first throws, then returns null (but throw happens first)
    assertThrows(IOException.class, () -> {
      try {
        handler.testHandleTypedFallback(originalException);
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    });
  }

  @Test
  void handleTypedFallback_shouldReturnNull_whenNoException() throws Throwable {
    // When no exception, should return null
    Object result = handler.testHandleTypedFallback(null);
    assertNull(result);
  }

  @Test
  void handleError_shouldLogAndThrowException() throws Throwable {
    IllegalArgumentException originalException = new IllegalArgumentException("Test error");

    assertThrows(IllegalArgumentException.class, () -> handler.testHandleError(originalException));
  }

  @Test
  void handleError_shouldThrowNullPointerException_whenNullThrowable() throws Throwable {
    assertThrows(NullPointerException.class, () -> handler.testHandleError(null));
  }

  // Test implementation of abstract class
  private static class TestCircuitBreakHandler extends AbstractCircuitBreakFallbackHandler {

    public void testHandleBodilessFallback(Throwable throwable) throws Throwable {
      handleBodilessFallback(throwable);
    }

    public Object testHandleTypedFallback(Throwable throwable) throws Throwable {
      return handleTypedFallback(throwable);
    }

    public void testHandleError(Throwable throwable) throws Throwable {
      handleError(throwable);
    }
  }
}