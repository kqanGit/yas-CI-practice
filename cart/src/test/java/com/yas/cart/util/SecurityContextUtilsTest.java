package com.yas.cart.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

class SecurityContextUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void setUpSecurityContext_setsAuthenticationName() {
        SecurityContextUtils.setUpSecurityContext("tester");

        assertEquals("tester", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
