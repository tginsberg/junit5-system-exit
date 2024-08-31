package com.ginsberg.junit.exit.assertions;

import com.ginsberg.junit.exit.ExitPreventerStrategy;
import com.ginsberg.junit.exit.SystemExitPreventedException;
import com.ginsberg.junit.exit.agent.AgentSystemExitHandlerStrategy;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class SystemExitAssertion {
    private final SystemExitPreventedException theException;

    public SystemExitAssertion(SystemExitPreventedException theException) {
        this.theException = theException;
    }

    public static SystemExitAssertion assertThatCodeCallsSystemExit(final Callable<?> callable) {
        return new SystemExitAssertion(catchSystemExitFrom(callable)).calledSystemExit();
    }

    public static void assertThatCodeDoesNotCallSystemExit(final Callable<?> callable) {
        new SystemExitAssertion(catchSystemExitFrom(callable)).didNotCallSystemExit();
    }

    private SystemExitAssertion calledSystemExit() {
        if (theException == null) {
            fail("Expected call to System.exit() did not happen");
        }
        return this;
    }

    private SystemExitAssertion didNotCallSystemExit() {
        if (theException != null) {
            fail("Unexpected call to System.exit() with exit code " + theException.getStatusCode(), theException);
        }
        return this;
    }

    private static SystemExitPreventedException catchSystemExitFrom(final Callable<?> callable) {
        final ExitPreventerStrategy exitPreventerStrategy = new AgentSystemExitHandlerStrategy();
        try {
            exitPreventerStrategy.beforeTest();
            callable.call();
            exitPreventerStrategy.afterTest();
        } catch (SystemExitPreventedException e) {
            return e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            exitPreventerStrategy.resetBetweenTests();
        }
        return null;
    }

    public SystemExitAssertion withExitCode(final int code) {
        assertEquals(code, theException.getStatusCode(), "Wrong exit code found");
        return this;
    }

    public SystemExitAssertion withExitCodeInRange(final int startInclusive, final int endInclusive) {
        assertTrue(startInclusive < endInclusive, "Start must come before end");
        final int code = theException.getStatusCode();
        assertTrue(
                startInclusive <= code && code <= endInclusive,
                "Exit code expected in range (" + startInclusive + " .. " + endInclusive + ") but was " + code
        );
        return this;
    }

}
