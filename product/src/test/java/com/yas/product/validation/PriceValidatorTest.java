package com.yas.product.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PriceValidatorTest {

    @Test
    void isValid_acceptsZeroAndPositive() {
        PriceValidator validator = new PriceValidator();

        assertTrue(validator.isValid(0.0, null));
        assertTrue(validator.isValid(25.5, null));
    }

    @Test
    void isValid_rejectsNegative() {
        PriceValidator validator = new PriceValidator();

        assertFalse(validator.isValid(-1.0, null));
    }

    @Test
    void isValid_throwsWhenNull() {
        PriceValidator validator = new PriceValidator();

        assertThrows(NullPointerException.class, () -> validator.isValid(null, null));
    }
}
