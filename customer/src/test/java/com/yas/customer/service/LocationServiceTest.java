package com.yas.customer.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.yas.customer.viewmodel.address.AddressDetailVm;
import com.yas.customer.viewmodel.address.AddressPostVm;
import com.yas.customer.viewmodel.address.AddressVm;
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
        "test", null, Collections.emptyList());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  // Test LocationService methods

  @Test
  void getAddressesByIdList_shouldSucceed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    var responseSpec = mock(org.springframework.web.client.RestClient.ResponseSpec.class);
    var requestHeadersUriSpec = mock(org.springframework.web.client.RestClient.RequestHeadersUriSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    var addressDetail = new AddressDetailVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, "District", 1L, "State", 1L, "USA");
    when(responseSpec.body(new org.springframework.core.ParameterizedTypeReference<List<AddressDetailVm>>() {}))
        .thenReturn(List.of(addressDetail));

    List<Long> ids = List.of(1L);
    var result = locationService.getAddressesByIdList(ids);

    assertDoesNotThrow(() -> result);
  }

  @Test
  void getAddressById_shouldSucceed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    var responseSpec = mock(org.springframework.web.client.RestClient.ResponseSpec.class);
    var requestHeadersUriSpec = mock(org.springframework.web.client.RestClient.RequestHeadersUriSpec.class);
    when(restClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(java.net.URI.class))).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.headers(any())).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);

    var addressDetail = new AddressDetailVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, "District", 1L, "State", 1L, "USA");
    when(responseSpec.body(AddressDetailVm.class)).thenReturn(addressDetail);

    var result = locationService.getAddressById(1L);

    assertDoesNotThrow(() -> result);
  }

  @Test
  void createAddress_shouldSucceed() {
    setSecurityContext();
    when(serviceUrlConfig.location()).thenReturn(LOCATION_URL);

    var responseSpec = mock(org.springframework.web.client.RestClient.ResponseSpec.class);
    var requestBodyUriSpec = mock(org.springframework.web.client.RestClient.RequestBodyUriSpec.class);
    when(restClient.post()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(any(java.net.URI.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.headers(any())).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.body(any(AddressPostVm.class))).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);

    var addressVm = new AddressVm(1L, "Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);
    when(responseSpec.body(AddressVm.class)).thenReturn(addressVm);

    var addressPostVm = new AddressPostVm("Contact", "1234567890", "123 Main St",
        "New York", "10001", 1L, 1L, 1L);
    var result = locationService.createAddress(addressPostVm);

    assertDoesNotThrow(() -> result);
  }

  // Test AbstractCircuitBreakFallbackHandler via LocationService inheritance

  @Test
  void fallbackHandler_throwsOriginalException() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    RuntimeException ex = new RuntimeException("test error");
    assertThrows(RuntimeException.class, () -> handler.testHandleTypedFallback(ex));
  }

  @Test
  void fallbackHandler_throwsIOException() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    java.io.IOException ex = new java.io.IOException("io error");
    assertThrows(java.io.IOException.class, () -> handler.testHandleTypedFallback(ex));
  }

  @Test
  void fallbackHandler_throwsIllegalArgument() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    IllegalArgumentException ex = new IllegalArgumentException("illegal");
    assertThrows(IllegalArgumentException.class, () -> handler.testHandleError(ex));
  }

  @Test
  void fallbackHandler_throwsOnNullThrowable() {
    TestCircuitBreakHandler handler = new TestCircuitBreakHandler();

    assertThrows(NullPointerException.class, () -> handler.testHandleError(null));
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