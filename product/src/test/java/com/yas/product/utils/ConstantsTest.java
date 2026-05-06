package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void errorCodes_haveExpectedValues() {
        assertEquals("PRODUCT_NOT_FOUND", Constants.ErrorCode.PRODUCT_NOT_FOUND);
        assertEquals("BRAND_NOT_FOUND", Constants.ErrorCode.BRAND_NOT_FOUND);
        assertNotNull(Constants.ErrorCode.MAKE_SURE_LENGTH_GREATER_THAN_WIDTH);
    }
}
