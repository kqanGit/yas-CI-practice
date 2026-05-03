package com.yas.cart.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CartItemViewModelTest {

    @Test
    void postVm_builder_setsFields() {
        CartItemPostVm vm = CartItemPostVm.builder()
            .productId(1L)
            .quantity(2)
            .build();

        assertEquals(1L, vm.productId());
        assertEquals(2, vm.quantity());
    }

    @Test
    void deleteVm_builder_setsFields() {
        CartItemDeleteVm vm = CartItemDeleteVm.builder()
            .productId(5L)
            .quantity(3)
            .build();

        assertEquals(5L, vm.productId());
        assertEquals(3, vm.quantity());
    }

    @Test
    void putVm_recordsQuantity() {
        CartItemPutVm vm = new CartItemPutVm(7);

        assertEquals(7, vm.quantity());
    }

    @Test
    void getVm_builder_setsFields() {
        CartItemGetVm vm = CartItemGetVm.builder()
            .customerId("u1")
            .productId(9L)
            .quantity(1)
            .build();

        assertEquals("u1", vm.customerId());
        assertEquals(9L, vm.productId());
        assertEquals(1, vm.quantity());
    }

    @Test
    void productThumbnailVm_builder_setsFields() {
        ProductThumbnailVm vm = ProductThumbnailVm.builder()
            .id(10L)
            .name("Name")
            .slug("slug")
            .thumbnailUrl("url")
            .build();

        assertEquals(10L, vm.id());
        assertEquals("slug", vm.slug());
    }
}
