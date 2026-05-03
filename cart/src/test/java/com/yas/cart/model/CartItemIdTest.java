package com.yas.cart.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class CartItemIdTest {

    @Test
    void equalsAndHashCode_useAllFields() {
        CartItemId left = new CartItemId("c1", 10L);
        CartItemId right = new CartItemId("c1", 10L);
        CartItemId other = new CartItemId("c1", 11L);

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
        assertNotEquals(left, other);
        assertNotEquals(left, new Object());
    }
}
