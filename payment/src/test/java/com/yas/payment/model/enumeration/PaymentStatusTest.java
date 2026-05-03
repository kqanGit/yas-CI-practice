package com.yas.payment.model.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PaymentStatusTest {

  @Test
  void paymentStatus_shouldHaveThreeValues() {
    // When
    PaymentStatus[] statuses = PaymentStatus.values();

    // Then
    assertEquals(3, statuses.length);
  }

  @Test
  void paymentStatus_shouldContainPending() {
    // When
    PaymentStatus pending = PaymentStatus.valueOf("PENDING");

    // Then
    assertNotNull(pending);
    assertEquals("PENDING", pending.name());
  }

  @Test
  void paymentStatus_shouldContainCompleted() {
    // When
    PaymentStatus completed = PaymentStatus.valueOf("COMPLETED");

    // Then
    assertNotNull(completed);
    assertEquals("COMPLETED", completed.name());
  }

  @Test
  void paymentStatus_shouldContainCancelled() {
    // When
    PaymentStatus cancelled = PaymentStatus.valueOf("CANCELLED");

    // Then
    assertNotNull(cancelled);
    assertEquals("CANCELLED", cancelled.name());
  }
}