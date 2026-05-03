package com.yas.payment.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PaymentTest {

  @Test
  void paymentBuilder_shouldCreatePaymentWithAllFields() {
    // Given
    Long orderId = 1L;
    String checkoutId = "checkout-123";
    BigDecimal amount = new BigDecimal("100.00");
    BigDecimal paymentFee = new BigDecimal("5.00");
    PaymentMethod paymentMethod = PaymentMethod.PAYPAL;
    PaymentStatus paymentStatus = PaymentStatus.PENDING;
    String gatewayTransactionId = "txn-456";
    String failureMessage = null;

    // When
    Payment payment = Payment.builder()
        .id(1L)
        .orderId(orderId)
        .checkoutId(checkoutId)
        .amount(amount)
        .paymentFee(paymentFee)
        .paymentMethod(paymentMethod)
        .paymentStatus(paymentStatus)
        .gatewayTransactionId(gatewayTransactionId)
        .failureMessage(failureMessage)
        .build();

    // Then
    assertEquals(1L, payment.getId());
    assertEquals(orderId, payment.getOrderId());
    assertEquals(checkoutId, payment.getCheckoutId());
    assertEquals(amount, payment.getAmount());
    assertEquals(paymentFee, payment.getPaymentFee());
    assertEquals(paymentMethod, payment.getPaymentMethod());
    assertEquals(paymentStatus, payment.getPaymentStatus());
    assertEquals(gatewayTransactionId, payment.getGatewayTransactionId());
    assertNull(payment.getFailureMessage());
  }

  @Test
  void paymentBuilder_shouldCreatePaymentWithDefaultValues() {
    // When
    Payment payment = Payment.builder().build();

    // Then
    assertNull(payment.getId());
    assertNull(payment.getOrderId());
    assertNull(payment.getCheckoutId());
    assertNull(payment.getAmount());
    assertNull(payment.getPaymentFee());
    assertNull(payment.getPaymentMethod());
    assertNull(payment.getPaymentStatus());
    assertNull(payment.getGatewayTransactionId());
    assertNull(payment.getFailureMessage());
  }

  @Test
  void noArgsConstructor_shouldCreateEmptyPayment() {
    // When
    Payment payment = new Payment();

    // Then
    assertNotNull(payment);
    assertNull(payment.getId());
    assertNull(payment.getOrderId());
    assertNull(payment.getCheckoutId());
    assertNull(payment.getAmount());
    assertNull(payment.getPaymentFee());
    assertNull(payment.getPaymentMethod());
    assertNull(payment.getPaymentStatus());
    assertNull(payment.getGatewayTransactionId());
    assertNull(payment.getFailureMessage());
  }

  @Test
  void allArgsConstructor_shouldCreatePaymentWithAllParameters() {
    // Given
    Long orderId = 2L;
    String checkoutId = "checkout-789";
    BigDecimal amount = new BigDecimal("250.50");
    BigDecimal paymentFee = new BigDecimal("10.00");
    PaymentMethod paymentMethod = PaymentMethod.COD;
    PaymentStatus paymentStatus = PaymentStatus.COMPLETED;
    String gatewayTransactionId = "txn-999";
    String failureMessage = "Payment failed";

    // When
    Payment payment = new Payment(
        orderId,
        checkoutId,
        amount,
        paymentFee,
        paymentMethod,
        paymentStatus,
        gatewayTransactionId,
        failureMessage
    );

    // Then - All args constructor does not set id, so id should be null
    assertNull(payment.getId());
    assertEquals(orderId, payment.getOrderId());
    assertEquals(checkoutId, payment.getCheckoutId());
    assertEquals(amount, payment.getAmount());
    assertEquals(paymentFee, payment.getPaymentFee());
    assertEquals(paymentMethod, payment.getPaymentMethod());
    assertEquals(paymentStatus, payment.getPaymentStatus());
    assertEquals(gatewayTransactionId, payment.getGatewayTransactionId());
    assertEquals(failureMessage, payment.getFailureMessage());
  }

  @Test
  void setters_shouldUpdatePaymentFields() {
    // Given
    Payment payment = new Payment();

    // When
    payment.setId(10L);
    payment.setOrderId(100L);
    payment.setCheckoutId("checkout-999");
    payment.setAmount(new BigDecimal("500.00"));
    payment.setPaymentFee(new BigDecimal("25.00"));
    payment.setPaymentMethod(PaymentMethod.BANKING);
    payment.setPaymentStatus(PaymentStatus.CANCELLED);
    payment.setGatewayTransactionId("txn-111");
    payment.setFailureMessage("Insufficient funds");

    // Then
    assertEquals(10L, payment.getId());
    assertEquals(100L, payment.getOrderId());
    assertEquals("checkout-999", payment.getCheckoutId());
    assertEquals(new BigDecimal("500.00"), payment.getAmount());
    assertEquals(new BigDecimal("25.00"), payment.getPaymentFee());
    assertEquals(PaymentMethod.BANKING, payment.getPaymentMethod());
    assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());
    assertEquals("txn-111", payment.getGatewayTransactionId());
    assertEquals("Insufficient funds", payment.getFailureMessage());
  }

  @Test
  void paymentBuilder_withCompletedStatus_shouldSetCorrectly() {
    // Given
    PaymentStatus status = PaymentStatus.COMPLETED;

    // When
    Payment payment = Payment.builder()
        .paymentStatus(status)
        .build();

    // Then
    assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
  }

  @Test
  void paymentBuilder_withCancelledStatus_shouldSetCorrectly() {
    // Given
    PaymentStatus status = PaymentStatus.CANCELLED;

    // When
    Payment payment = Payment.builder()
        .paymentStatus(status)
        .build();

    // Then
    assertEquals(PaymentStatus.CANCELLED, payment.getPaymentStatus());
  }

  @Test
  void paymentBuilder_withCodMethod_shouldSetCorrectly() {
    // Given
    PaymentMethod method = PaymentMethod.COD;

    // When
    Payment payment = Payment.builder()
        .paymentMethod(method)
        .build();

    // Then
    assertEquals(PaymentMethod.COD, payment.getPaymentMethod());
  }

  @Test
  void paymentBuilder_withBankingMethod_shouldSetCorrectly() {
    // Given
    PaymentMethod method = PaymentMethod.BANKING;

    // When
    Payment payment = Payment.builder()
        .paymentMethod(method)
        .build();

    // Then
    assertEquals(PaymentMethod.BANKING, payment.getPaymentMethod());
  }

  @Test
  void paymentBuilder_withPaypalMethod_shouldSetCorrectly() {
    // Given
    PaymentMethod method = PaymentMethod.PAYPAL;

    // When
    Payment payment = Payment.builder()
        .paymentMethod(method)
        .build();

    // Then
    assertEquals(PaymentMethod.PAYPAL, payment.getPaymentMethod());
  }

  @Test
  void payment_withFailureMessage_shouldStoreMessage() {
    // Given
    String errorMessage = "Card declined";

    // When
    Payment payment = Payment.builder()
        .failureMessage(errorMessage)
        .build();

    // Then
    assertEquals(errorMessage, payment.getFailureMessage());
  }

  @Test
  void payment_withZeroAmount_shouldHandleCorrectly() {
    // Given
    BigDecimal zeroAmount = BigDecimal.ZERO;

    // When
    Payment payment = Payment.builder()
        .amount(zeroAmount)
        .build();

    // Then
    assertEquals(BigDecimal.ZERO, payment.getAmount());
  }

  @Test
  void payment_withLargeAmount_shouldHandleCorrectly() {
    // Given
    BigDecimal largeAmount = new BigDecimal("999999999.99");

    // When
    Payment payment = Payment.builder()
        .amount(largeAmount)
        .build();

    // Then
    assertEquals(new BigDecimal("999999999.99"), payment.getAmount());
  }
}