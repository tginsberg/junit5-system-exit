package com.ginsberg.junit.exit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DisallowExitSecurityManagerTest {

    @Test
    @DisplayName("Captures first exit status code only")
    void capturesFirstExitCode() {
        final DisallowExitSecurityManager securityManager = new DisallowExitSecurityManager(null);
        exit(securityManager, 1);
        exit(securityManager, 2);
        assertEquals(Integer.valueOf(1), securityManager.getFirstExitStatusCode());
    }

    private void exit(final SecurityManager securityManager, int code) {
        try {
            securityManager.checkExit(code);
        } catch (final SystemExitPreventedException e) {
            // Gulp!
        }
    }

}