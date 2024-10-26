/*
 * MIT License
 *
 * Copyright (c) 2024 Todd Ginsberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.ginsberg.junit.exit.assertions;

import com.ginsberg.junit.exit.ExitPreventerStrategy;
import com.ginsberg.junit.exit.SystemExitPreventedException;
import com.ginsberg.junit.exit.agent.AgentSystemExitHandlerStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class SystemExitAssertion {
    private final SystemExitPreventedException theException;

    public SystemExitAssertion(SystemExitPreventedException theException) {
        this.theException = theException;
    }

    public static SystemExitAssertion assertThatCallsSystemExit(final Runnable function) {
        return new SystemExitAssertion(catchSystemExitFrom(function)).calledSystemExit();
    }

    public static void assertThatDoesNotCallSystemExit(final Runnable function) {
        new SystemExitAssertion(catchSystemExitFrom(function)).didNotCallSystemExit();
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

    private static SystemExitPreventedException catchSystemExitFrom(final Runnable function) {
        final ExitPreventerStrategy exitPreventerStrategy = new AgentSystemExitHandlerStrategy();
        try {
            exitPreventerStrategy.resetBetweenTests();
            exitPreventerStrategy.beforeTest();
            function.run();
        } catch (SystemExitPreventedException e) {
            return e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            exitPreventerStrategy.afterTest();
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
