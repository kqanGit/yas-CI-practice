package com.yas.cart.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCodes_matchExpectedValues() {
        assertEquals("NOT_FOUND_PRODUCT", Constants.ErrorCode.NOT_FOUND_PRODUCT);
        assertEquals("ADD_CART_ITEM_FAILED", Constants.ErrorCode.ADD_CART_ITEM_FAILED);
        assertNotNull(Constants.ErrorCode.DUPLICATED_CART_ITEMS_TO_DELETE);
    }
}
