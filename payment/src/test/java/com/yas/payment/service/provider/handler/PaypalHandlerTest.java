package com.yas.payment.service.provider.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.InitiatedPayment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentResponse;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentResponse;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaypalHandlerTest {

    @Mock
    private PaymentProviderService paymentProviderService;

    @Mock
    private PaypalService paypalService;

    @InjectMocks
    private PaypalHandler paypalHandler;

    @Test
    void getProviderId_shouldReturnPaypal() {
        assertEquals("PAYPAL", paypalHandler.getProviderId());
    }

    @Test
    void initPayment_shouldReturnInitiatedPayment() {
        // Given
        InitPaymentRequestVm request = InitPaymentRequestVm.builder()
            .paymentMethod("PAYPAL")
            .totalPrice(BigDecimal.valueOf(100))
            .checkoutId("checkout-123")
            .build();

        PaypalCreatePaymentResponse paypalResponse = PaypalCreatePaymentResponse.builder()
            .status("CREATED")
            .paymentId("pay-001")
            .redirectUrl("https://paypal.com/pay")
            .build();

        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL"))
            .thenReturn("{\"clientId\":\"abc\"}");
        when(paypalService.createPayment(any(PaypalCreatePaymentRequest.class))).thenReturn(paypalResponse);

        // When
        InitiatedPayment response = paypalHandler.initPayment(request);

        // Then
        assertNotNull(response);
        assertEquals("CREATED", response.getStatus());
        assertEquals("pay-001", response.getPaymentId());
        assertEquals("https://paypal.com/pay", response.getRedirectUrl());
    }

    @Test
    void capturePayment_shouldReturnCapturedPayment() {
        // Given
        CapturePaymentRequestVm request = CapturePaymentRequestVm.builder()
            .paymentMethod("PAYPAL")
            .token("token-123")
            .build();

        PaypalCapturePaymentResponse paypalResponse = PaypalCapturePaymentResponse.builder()
            .checkoutId("checkout-123")
            .amount(BigDecimal.valueOf(100))
            .paymentFee(BigDecimal.valueOf(3))
            .gatewayTransactionId("txn-001")
            .paymentMethod("PAYPAL")
            .paymentStatus("COMPLETED")
            .failureMessage(null)
            .build();

        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL"))
            .thenReturn("{\"clientId\":\"abc\"}");
        when(paypalService.capturePayment(any(PaypalCapturePaymentRequest.class))).thenReturn(paypalResponse);

        // When
        CapturedPayment response = paypalHandler.capturePayment(request);

        // Then
        assertNotNull(response);
        assertEquals("checkout-123", response.getCheckoutId());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        assertEquals(BigDecimal.valueOf(3), response.getPaymentFee());
        assertEquals("txn-001", response.getGatewayTransactionId());
        assertEquals(PaymentMethod.PAYPAL, response.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, response.getPaymentStatus());
    }
}
