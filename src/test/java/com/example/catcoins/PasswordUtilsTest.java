package com.example.catcoins;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void hashPassword() {
        try {
            assertNotEquals("Teste",PasswordUtils.hashPassword("Teste"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}