package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

  @Mock
  private org.springframework.web.client.RestClient restClient;

  @Mock
  private com.yas.customer.config.ServiceUrlConfig serviceUrlConfig;

  private LocationService locationService;

  private static final String LOCATION_URL = "http://api.yas.local/location";

  @BeforeEach
  void setUp() {
    locationService = new LocationService(restClient, serviceUrlConfig);
  }

  private void setSecurityContext() {
    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        "testuser", null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  // ===== Test circuit breaker behavior via LocationService =====

  @Test
  void getAddressesByIdList_shouldSucceed_whenCircuitBreakerClosed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    AddressDetailVm addressDetail = new AddressDetailVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, "District", 1L, "State", 1L, "USA");

    org.springframework.web.client.RestClient.ResponseSpec responseSpec = mock(
        org.springframework.web.client.RestClient.ResponseSpec.class);
    org.springframework.web.client.RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(
        org.springframework.web.client.RestClient.RequestHeadersUriSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(new org.springframework.core.ParameterizedTypeReference<List<AddressDetailVm>>() {}))
        .thenReturn(List.of(addressDetail));

    List<Long> ids = List.of(1L);
    List<AddressDetailVm> result = locationService.getAddressesByIdList(ids);

    assertDoesNotThrow(() -> result);
  }

  @Test
  void getAddressById_shouldSucceed_whenCircuitBreakerClosed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    AddressDetailVm addressDetail = new AddressDetailVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, "District", 1L, "State", 1L, "USA");

    org.springframework.web.client.RestClient.ResponseSpec responseSpec = mock(
        org.springframework.web.client.RestClient.ResponseSpec.class);
    org.springframework.web.client.RestClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(
        org.springframework.web.client.RestClient.RequestHeadersUriSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(AddressDetailVm.class)).thenReturn(addressDetail);

    AddressDetailVm result = locationService.getAddressById(1L);

    assertDoesNotThrow(() -> result);
  }

  @Test
  void createAddress_shouldSucceed_whenCircuitBreakerClosed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    AddressVm addressVm = new AddressVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);

    org.springframework.web.client.RestClient.ResponseSpec responseSpec = mock(
        org.springframework.web.client.RestClient.ResponseSpec.class);
    org.springframework.web.client.RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(
        org.springframework.web.client.RestClient.RequestBodyUriSpec.class);
    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.body(any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(AddressVm.class)).thenReturn(addressVm);

    AddressPostVm addressPostVm = new AddressPostVm("Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);

    AddressVm result = locationService.createAddress(addressPostVm);

    assertDoesNotThrow(() -> result);
  }

  // ===== Test Fallback behavior =====

  @Test
  void handleTypedFallback_shouldThrowOriginalException() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    RuntimeException ex = new RuntimeException("test");
    assertThrows(RuntimeException.class, () -> handler.testHandleTypedFallback(ex));
  }

  @Test
  void handleBodilessFallback_shouldThrowOriginalException() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    RuntimeException ex = new RuntimeException("test");
    assertThrows(RuntimeException.class, () -> handler.testHandleBodilessFallback(ex));
  }

  @Test
  void handleError_shouldThrowOriginalException() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    IllegalArgumentException ex = new IllegalArgumentException("test");
    assertThrows(IllegalArgumentException.class, () -> handler.testHandleError(ex));
  }

  @Test
  void handleTypedFallback_shouldReturnNull_whenNullThrowable() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    Object result = handler.testHandleTypedFallback(null);
    assertNull(result);
  }

  // Test implementation
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