package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AbstractCircuitBreakFallbackHandlerTest {

  private TestCircuitBreakHandler handler;

  @BeforeEach
  void setUp() {
    handler = new TestCircuitBreakHandler();
  }

  @Test
  void handleError_shouldThrowOriginalException() {
    RuntimeException ex = new RuntimeException("test error");
    assertThrows(RuntimeException.class, () -> handler.testHandleError(ex));
  }

  @Test
  void handleError_shouldThrowIOException() {
    java.io.IOException ex = new java.io.IOException("io error");
    assertThrows(java.io.IOException.class, () -> handler.testHandleError(ex));
  }

  @Test
  void handleError_shouldThrowIllegalArgument() {
    IllegalArgumentException ex = new IllegalArgumentException("illegal");
    assertThrows(IllegalArgumentException.class, () -> handler.testHandleError(ex));
  }

  @Test
  void handleError_shouldThrowNullPointer_whenNullThrowable() {
    assertThrows(NullPointerException.class, () -> handler.testHandleError(null));
  }

  @Test
  void handleTypedFallback_shouldThrow_whenException() {
    RuntimeException ex = new RuntimeException("test");
    assertThrows(RuntimeException.class, () -> handler.testHandleTypedFallback(ex));
  }

  @Test
  void handleTypedFallback_shouldReturnNull_whenNoException() {
    // When null is passed, returns null instead of throwing
    Object result = handler.testHandleTypedFallback(null);
    assertNull(result);
  }

  // Test implementation
  private static class TestCircuitBreakHandler extends AbstractCircuitBreakFallbackHandler {
    public Object testHandleTypedFallback(Throwable throwable) throws Throwable {
      return handleTypedFallback(throwable);
    }
    public void testHandleError(Throwable throwable) throws Throwable {
      handleError(throwable);
    }
  }
}