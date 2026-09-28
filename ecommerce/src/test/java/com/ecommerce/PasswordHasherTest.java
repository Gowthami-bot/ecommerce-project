package com.ecommerce;

import com.ecommerce.security.PasswordHasher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashesAndVerifiesPasswordsWithoutStoringPlaintext() {
        String hash = passwordHasher.hash("sample-password-2026");

        assertNotEquals("sample-password-2026", hash);
        assertTrue(passwordHasher.matches("sample-password-2026", hash));
        assertFalse(passwordHasher.matches("incorrect-password", hash));
        assertFalse(passwordHasher.needsUpgrade(hash));
    }

    @Test
    void recognizesLegacyPlaintextForUpgradeAfterSuccessfulLogin() {
        assertTrue(passwordHasher.matches("legacy-password", "legacy-password"));
        assertTrue(passwordHasher.needsUpgrade("legacy-password"));
    }
}