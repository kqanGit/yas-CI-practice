package com.yas.cart.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.yas.cart.model.CartItem;
import com.yas.cart.viewmodel.CartItemGetVm;
import com.yas.cart.viewmodel.CartItemPostVm;
import java.util.List;
import org.junit.jupiter.api.Test;

class CartItemMapperTest {

    private final CartItemMapper cartItemMapper = new CartItemMapper();

    @Test
    void toGetVm_mapsFields() {
        CartItem cartItem = CartItem.builder()
            .customerId("user-1")
            .productId(10L)
            .quantity(2)
            .build();

        CartItemGetVm vm = cartItemMapper.toGetVm(cartItem);

        assertEquals("user-1", vm.customerId());
        assertEquals(10L, vm.productId());
        assertEquals(2, vm.quantity());
    }

    @Test
    void toCartItem_fromPostVm_mapsFields() {
        CartItemPostVm postVm = CartItemPostVm.builder()
            .productId(11L)
            .quantity(3)
            .build();

        CartItem cartItem = cartItemMapper.toCartItem(postVm, "user-2");

        assertEquals("user-2", cartItem.getCustomerId());
        assertEquals(11L, cartItem.getProductId());
        assertEquals(3, cartItem.getQuantity());
    }

    @Test
    void toCartItem_fromExplicitValues_mapsFields() {
        CartItem cartItem = cartItemMapper.toCartItem("user-3", 12L, 5);

        assertEquals("user-3", cartItem.getCustomerId());
        assertEquals(12L, cartItem.getProductId());
        assertEquals(5, cartItem.getQuantity());
    }

    @Test
    void toGetVms_mapsList() {
        List<CartItem> items = List.of(
            CartItem.builder().customerId("u1").productId(1L).quantity(1).build(),
            CartItem.builder().customerId("u1").productId(2L).quantity(2).build()
        );

        List<CartItemGetVm> vms = cartItemMapper.toGetVms(items);

        assertNotNull(vms);
        assertEquals(2, vms.size());
        assertEquals(2L, vms.get(1).productId());
    }
}
