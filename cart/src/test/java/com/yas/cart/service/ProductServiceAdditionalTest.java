package com.yas.cart.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import com.yas.cart.viewmodel.ProductThumbnailVm;
import com.yas.commonlibrary.config.ServiceUrlConfig;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

class ProductServiceAdditionalTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;

    @BeforeEach
    void setUp() {
        restClient = Mockito.mock(RestClient.class);
        serviceUrlConfig = Mockito.mock(ServiceUrlConfig.class);
    }

    @Test
    void getProductById_returnsNullWhenEmptyList() {
        ProductService productService = spy(new ProductService(restClient, serviceUrlConfig));
        doReturn(List.of()).when(productService).getProducts(List.of(10L));

        ProductThumbnailVm result = productService.getProductById(10L);

        assertNull(result);
    }

    @Test
    void existsById_returnsTrueWhenFound() {
        ProductService productService = spy(new ProductService(restClient, serviceUrlConfig));
        ProductThumbnailVm vm = new ProductThumbnailVm(1L, "P", "p", "u");
        doReturn(List.of(vm)).when(productService).getProducts(List.of(1L));

        assertTrue(productService.existsById(1L));
    }

    @Test
    void existsById_returnsFalseWhenMissing() {
        ProductService productService = spy(new ProductService(restClient, serviceUrlConfig));
        doReturn(List.of()).when(productService).getProducts(List.of(2L));

        assertFalse(productService.existsById(2L));
    }
}
