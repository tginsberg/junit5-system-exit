package com.ginsberg.junit.exit.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatCodeCallsSystemExit;
import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatCodeDoesNotCallSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SystemExitAssertionTest {

    @Test
    void catchesExit() {
        assertThatCodeCallsSystemExit(this::justExit);
    }

    @Test
    void catchesExitWithCode() {
        assertThatCodeCallsSystemExit(this::justExit)
                .withExitCode(42);
    }

    @Test
    void catchesExitWithCodeInRange() {
        assertThatCodeCallsSystemExit(this::justExit)
                .withExitCodeInRange(41, 43);
    }

    @Test
    void catchesMultipleExits() {
        assertThatCodeCallsSystemExit(() -> {
            justExit();
            justExit();
            justExit();
            return null;
        }).withExitCode(42);
    }

    @Test
    void exitCodeDoesNotMatch() {
        try {
            assertThatCodeCallsSystemExit(() -> null).withExitCode(43);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void exitCodeNotInRangeHigh() {
        try {
            assertThatCodeCallsSystemExit(() -> null).withExitCode(44);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void exitCodeNotInRangeLow() {
        try {
            assertThatCodeCallsSystemExit(() -> null).withExitCode(41);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void expectingNoExit() {
        assertThatCodeDoesNotCallSystemExit(() -> null);
    }

    @Test
    void expectingNoExitWhenExitHappens() {
        try {
            assertThatCodeDoesNotCallSystemExit(() -> {
                System.exit(42);
                return null;
            });
            fail("Should have failed test when System.exit was called but not expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void expectingSystemExitButSomethingElseThrown() {
        try {
            assertThatCodeCallsSystemExit(() -> {
                throw new IllegalStateException();
            }).withExitCode(42);
        } catch(final Exception e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

    @Test
    void failsWhenNoExit() {
        try {
            assertThatCodeCallsSystemExit(() -> null).withExitCode(42);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    private Object justExit() {
        System.exit(42);
        return null;
    }
}