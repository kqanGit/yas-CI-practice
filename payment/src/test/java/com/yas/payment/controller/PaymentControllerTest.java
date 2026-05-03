package com.yas.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        // Init via @InjectMocks
    }

    @Test
    void initPayment_shouldReturnInitPaymentResponse() {
        // Given
        InitPaymentRequestVm request = InitPaymentRequestVm.builder()
            .paymentMethod("PAYPAL")
            .totalPrice(BigDecimal.valueOf(100))
            .checkoutId("checkout-123")
            .build();

        InitPaymentResponseVm expectedResponse = InitPaymentResponseVm.builder()
            .status("CREATED")
            .paymentId("pay-001")
            .redirectUrl("https://paypal.com/pay")
            .build();

        when(paymentService.initPayment(any(InitPaymentRequestVm.class))).thenReturn(expectedResponse);

        // When
        InitPaymentResponseVm response = paymentController.initPayment(request);

        // Then
        assertNotNull(response);
        assertEquals("CREATED", response.status());
        assertEquals("pay-001", response.paymentId());
        assertEquals("https://paypal.com/pay", response.redirectUrl());
    }

    @Test
    void capturePayment_shouldReturnCapturePaymentResponse() {
        // Given
        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
            .paymentMethod("PAYPAL")
            .token("token-123")
            .build();

        CapturePaymentResponseVm expectedResponse = CapturePaymentResponseVm.builder()
            .orderId(1L)
            .checkoutId("checkout-123")
            .amount(BigDecimal.valueOf(100))
            .paymentFee(BigDecimal.valueOf(3))
            .gatewayTransactionId("txn-001")
            .paymentMethod(PaymentMethod.PAYPAL)
            .paymentStatus(PaymentStatus.COMPLETED)
            .failureMessage(null)
            .build();

        when(paymentService.capturePayment(any(CapturePaymentRequestVm.class))).thenReturn(expectedResponse);

        // When
        CapturePaymentResponseVm response = paymentController.capturePayment(request);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.orderId());
        assertEquals("checkout-123", response.checkoutId());
        assertEquals(BigDecimal.valueOf(100), response.amount());
        assertEquals(PaymentMethod.PAYPAL, response.paymentMethod());
        assertEquals(PaymentStatus.COMPLETED, response.paymentStatus());
    }

    @Test
    void cancelPayment_shouldReturnOkWithMessage() {
        // When
        ResponseEntity<String> response = paymentController.cancelPayment();

        // Then
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Payment cancelled", response.getBody());
    }
}
