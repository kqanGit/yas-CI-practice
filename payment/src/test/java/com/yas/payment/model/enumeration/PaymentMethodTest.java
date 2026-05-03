package com.yas.payment.model.enumeration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PaymentMethodTest {

  @Test
  void paymentMethod_shouldHaveThreeValues() {
    // When
    PaymentMethod[] methods = PaymentMethod.values();

    // Then
    assertEquals(3, methods.length);
  }

  @Test
  void paymentMethod_shouldContainCod() {
    // When
    PaymentMethod cod = PaymentMethod.valueOf("COD");

    // Then
    assertNotNull(cod);
    assertEquals("COD", cod.name());
  }

  @Test
  void paymentMethod_shouldContainBanking() {
    // When
    PaymentMethod banking = PaymentMethod.valueOf("BANKING");

    // Then
    assertNotNull(banking);
    assertEquals("BANKING", banking.name());
  }

  @Test
  void paymentMethod_shouldContainPaypal() {
    // When
    PaymentMethod paypal = PaymentMethod.valueOf("PAYPAL");

    // Then
    assertNotNull(paypal);
    assertEquals("PAYPAL", paypal.name());
  }
}