package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_formatsKnownKey() {
        String message = MessagesUtils.getMessage(Constants.ErrorCode.PRODUCT_NOT_FOUND, "P-100");
        assertEquals("Product P-100 is not found", message);
    }

    @Test
    void getMessage_returnsCodeWhenMissingKey() {
        String message = MessagesUtils.getMessage("UNKNOWN_CODE", "ignored");
        assertEquals("UNKNOWN_CODE", message);
    }

    @Test
    void getMessage_keepsTemplateWhenNoArgs() {
        String message = MessagesUtils.getMessage(Constants.ErrorCode.PRODUCT_NOT_FOUND);
        assertEquals("Product {} is not found", message);
    }
}
