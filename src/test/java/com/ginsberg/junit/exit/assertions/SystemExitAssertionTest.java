package com.ginsberg.junit.exit.assertions;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatCallsSystemExit;
import static com.ginsberg.junit.exit.assertions.SystemExitAssertion.assertThatDoesNotCallSystemExit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class SystemExitAssertionTest {

    @Test
    void catchesExit() {
        assertThatCallsSystemExit(() -> System.exit(1));
    }

    @Test
    void catchesExitWithCode() {
        assertThatCallsSystemExit(() -> System.exit(2)).withExitCode(2);
    }

    @Test
    void catchesExitWithCodeInRange() {
        assertThatCallsSystemExit(() -> System.exit(3)).withExitCodeInRange(1, 3);
    }

    @Test
    void catchesMultipleExits() {
        assertThatCallsSystemExit(() -> {
            System.exit(4);
            System.exit(5);
            System.exit(6);
        }).withExitCode(4);
    }

    @Test
    void exitCodeDoesNotMatch() {
        try {
            assertThatCallsSystemExit(() -> System.exit(5)).withExitCode(6);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            assertThat(e.getMessage()).startsWith("Wrong exit code found");
        }
    }

    @Test
    void exitCodeNotInRangeHigh() {
        try {
            assertThatCallsSystemExit(() -> System.exit(7)).withExitCodeInRange(1, 6);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            assertThat(e.getMessage()).startsWith("Exit code expected in range (1 .. 6) but was 7");
        }
    }

    @Test
    void exitCodeNotInRangeLow() {
        try {
            assertThatCallsSystemExit(() -> System.exit(8)).withExitCodeInRange(9, 11);
            fail("Should have failed test when System.exit was not in range");
        } catch (AssertionFailedError e) {
            assertThat(e.getMessage()).startsWith("Exit code expected in range (9 .. 11) but was 8");
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
                    System.exit(9)
            );
            fail("Should have failed test when System.exit was called but not expected");
        } catch (AssertionFailedError e) {
            assertThat(e.getMessage()).startsWith("Unexpected call to System.exit()");
        }
    }

    @Test
    void expectingSystemExitButSomethingElseThrown() {
        try {
            assertThatCallsSystemExit(() -> {
                throw new IllegalStateException();
            }).withExitCode(10);
        } catch (final Exception e) {
            assertEquals(IllegalStateException.class, e.getCause().getClass());
        }
    }

    @Test
    void failsWhenNoExit() {
        try {
            assertThatCallsSystemExit(System::currentTimeMillis).withExitCode(11);
            fail("Should have failed test when System.exit was not called but expected");
        } catch (AssertionFailedError e) {
            assertThat(e.getMessage()).startsWith("Expected call to System.exit() did not happen");
        }
    }
}