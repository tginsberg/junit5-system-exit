package com.ginsberg.junit.exit.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatCallsSystemExit;
import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatDoesNotCallSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SystemExitAssertionTest {

    @Test
    void catchesExit() {
        assertThatCallsSystemExit(() -> System.exit(42));
    }

    @Test
    void catchesExitWithCode() {
        assertThatCallsSystemExit(() -> System.exit(42)).withExitCode(42);
    }

    @Test
    void catchesExitWithCodeInRange() {
        assertThatCallsSystemExit(() -> System.exit(42)).withExitCodeInRange(41, 43);
    }

    @Test
    void catchesMultipleExits() {
        assertThatCallsSystemExit(() -> {
            justExit();
            justExit();
            justExit();
        }).withExitCode(42);
    }

    @Test
    void exitCodeDoesNotMatch() {
        try {
            assertThatCallsSystemExit(() -> System.exit(42)).withExitCode(43);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void exitCodeNotInRangeHigh() {
        try {
            assertThatCallsSystemExit(() -> System.exit(44)).withExitCodeInRange(41, 43);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void exitCodeNotInRangeLow() {
        try {
            assertThatCallsSystemExit(() -> System.exit(40)).withExitCodeInRange(41,43);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void expectingNoExit() {
        assertThatDoesNotCallSystemExit(System::currentTimeMillis);
    }

    @Test
    void expectingNoExitWhenExitHappens() {
        try {
            assertThatDoesNotCallSystemExit(() ->
                System.exit(42)
            );
            fail("Should have failed test when System.exit was called but not expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    @Test
    void expectingSystemExitButSomethingElseThrown() {
        try {
            assertThatCallsSystemExit(() -> {
                throw new IllegalStateException();
            }).withExitCode(42);
        } catch(final Exception e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

    @Test
    void failsWhenNoExit() {
        try {
            assertThatCallsSystemExit(System::currentTimeMillis).withExitCode(42);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            // Expected
        }
    }

    private void justExit() {
        System.exit(42);
    }
}