package com.yas.payment.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class PaymentProviderControllerTest {

    @Mock
    private PaymentProviderService paymentProviderService;

    @InjectMocks
    private PaymentProviderController paymentProviderController;

    @BeforeEach
    void setUp() {
        // Init via @InjectMocks
    }

    @Test
    void create_shouldReturnCreatedPaymentProvider() {
        // Given
        CreatePaymentVm request = new CreatePaymentVm();
        request.setId("paypal");
        request.setName("PayPal");
        request.setConfigureUrl("https://paypal.com/config");
        request.setEnabled(true);

        PaymentProviderVm expectedResponse = new PaymentProviderVm(
            "paypal", "PayPal", "https://paypal.com/config", 1, null, null
        );

        when(paymentProviderService.create(any(CreatePaymentVm.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<PaymentProviderVm> responseEntity = paymentProviderController.create(request);

        // Then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        PaymentProviderVm responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("paypal", responseBody.getId());
        assertEquals("PayPal", responseBody.getName());
        assertEquals("https://paypal.com/config", responseBody.getConfigureUrl());
    }

    @Test
    void update_shouldReturnUpdatedPaymentProvider() {
        // Given
        UpdatePaymentVm request = new UpdatePaymentVm();
        request.setId("paypal");
        request.setName("PayPal Updated");
        request.setConfigureUrl("https://paypal.com/config-v2");
        request.setEnabled(true);

        PaymentProviderVm expectedResponse = new PaymentProviderVm(
            "paypal", "PayPal Updated", "https://paypal.com/config-v2", 2, null, null
        );

        when(paymentProviderService.update(any(UpdatePaymentVm.class))).thenReturn(expectedResponse);

        // When
        ResponseEntity<PaymentProviderVm> responseEntity = paymentProviderController.update(request);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        PaymentProviderVm responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("paypal", responseBody.getId());
        assertEquals("PayPal Updated", responseBody.getName());
        assertEquals("https://paypal.com/config-v2", responseBody.getConfigureUrl());
    }

    @Test
    void getAll_shouldReturnListOfEnabledProviders() {
        // Given
        PaymentProviderVm provider1 = new PaymentProviderVm(
            "paypal", "PayPal", "https://paypal.com/config", 1, 100L, "https://media.com/paypal.png"
        );
        PaymentProviderVm provider2 = new PaymentProviderVm(
            "cod", "Cash on Delivery", "https://cod.com/config", 1, 200L, "https://media.com/cod.png"
        );

        when(paymentProviderService.getEnabledPaymentProviders(any(Pageable.class)))
            .thenReturn(List.of(provider1, provider2));

        // When
        ResponseEntity<List<PaymentProviderVm>> responseEntity = paymentProviderController.getAll(Pageable.unpaged());

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        List<PaymentProviderVm> body = responseEntity.getBody();
        assertNotNull(body);
        assertEquals(2, body.size());
        assertEquals("paypal", body.get(0).getId());
        assertEquals("PayPal", body.get(0).getName());
        assertEquals("cod", body.get(1).getId());
        assertEquals("Cash on Delivery", body.get(1).getName());
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoProvidersEnabled() {
        // Given
        when(paymentProviderService.getEnabledPaymentProviders(any(Pageable.class)))
            .thenReturn(List.of());

        // When
        ResponseEntity<List<PaymentProviderVm>> responseEntity = paymentProviderController.getAll(Pageable.unpaged());

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        List<PaymentProviderVm> body = responseEntity.getBody();
        assertNotNull(body);
        assertTrue(body.isEmpty());
    }
}
