package com.yas.product.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class ModelEqualityTest {

    @Test
    void brandEqualityAndHashCode() throws Exception {
        verifyEqualityContract(Brand.class);
    }

    @Test
    void categoryEqualityAndHashCode() throws Exception {
        verifyEqualityContract(Category.class);
    }

    @Test
    void productEqualityAndHashCode() throws Exception {
        verifyEqualityContract(Product.class);
    }

    @Test
    void productOptionEqualityAndHashCode() throws Exception {
        verifyEqualityContract(ProductOption.class);
    }

    private <T> void verifyEqualityContract(Class<T> type) throws Exception {
        T left = type.getDeclaredConstructor().newInstance();
        T right = type.getDeclaredConstructor().newInstance();
        T other = type.getDeclaredConstructor().newInstance();
        T noId = type.getDeclaredConstructor().newInstance();

        Method setId = type.getMethod("setId", Long.class);
        setId.invoke(left, 1L);
        setId.invoke(right, 1L);
        setId.invoke(other, 2L);

        assertEquals(left, right);
        assertNotEquals(left, other);
        assertNotEquals(left, noId);
        assertNotEquals(left, new Object());
        assertEquals(type.hashCode(), left.hashCode());
    }
}
