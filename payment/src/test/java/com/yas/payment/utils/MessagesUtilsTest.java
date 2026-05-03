package com.yas.payment.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_withValidKey_shouldReturnFormattedMessage() {
        // When the key exists in messages.properties, it returns the formatted message
        // When the key doesn't exist, it returns the key itself
        String result = MessagesUtils.getMessage("NON_EXISTING_KEY");
        assertEquals("NON_EXISTING_KEY", result);
    }

    @Test
    void getMessage_withNonExistingKey_shouldReturnKeyAsMessage() {
        // Given a key that doesn't exist in the resource bundle
        String errorCode = "SOME_RANDOM_ERROR_CODE";

        // When
        String result = MessagesUtils.getMessage(errorCode);

        // Then - should return the error code itself
        assertEquals(errorCode, result);
    }

    @Test
    void getMessage_withArguments_shouldFormatMessage() {
        // Given a key that doesn't exist (returns the key as template)
        String errorCode = "Error occurred for {} with id {}";

        // When
        String result = MessagesUtils.getMessage(errorCode, "payment", "123");

        // Then - MessageFormatter should replace {} with arguments
        assertEquals("Error occurred for payment with id 123", result);
    }

    @Test
    void getMessage_withNoArguments_shouldReturnMessageAsIs() {
        // Given
        String errorCode = "SIMPLE_ERROR";

        // When
        String result = MessagesUtils.getMessage(errorCode);

        // Then
        assertEquals("SIMPLE_ERROR", result);
    }
}
