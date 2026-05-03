package com.yas.product.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProductConverterTest {

    @Test
    void toSlug_trimsAndNormalizes() {
        String result = ProductConverter.toSlug("  Hello World  ");
        assertEquals("hello-world", result);
    }

    @Test
    void toSlug_collapsesHyphens() {
        String result = ProductConverter.toSlug("A---B----C");
        assertEquals("a-b-c", result);
    }

    @Test
    void toSlug_handlesOnlySymbols() {
        String result = ProductConverter.toSlug("@@@###");
        assertEquals("", result);
    }

    @Test
    void toSlug_throwsOnNull() {
        assertThrows(NullPointerException.class, () -> ProductConverter.toSlug(null));
    }
}
