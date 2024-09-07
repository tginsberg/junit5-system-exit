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

package com.ginsberg.junit.exit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.ginsberg.junit.exit.TestUtils.assertTestFails;
import static com.ginsberg.junit.exit.TestUtils.assertTestSucceeds;

class FailOnSystemExitTest {

    @Test
    @DisplayName("@FailOnSystemExit on method - exception caught and fails test")
    void failOnSystemExitOnMethod() {
        assertTestFails(FailOnSystemExitAtTestLevelTest.class, "callsSystemExit");
    }

    @Test
    @DisplayName("@FailOnSystemExit on method - System.exit not called")
    void succeedWhenNotCallingSystemExitInMethod() {
        assertTestSucceeds(FailOnSystemExitAtTestLevelTest.class, "doesNotCallSystemExit");
    }

    @Test
    @DisplayName("@FailOnSystemExit on class - exception caught and fails test")
    void failOnSystemExitOnClass() {
        assertTestFails(FailOnSystemExitAtClassLevelTest.class);
    }

    @Test
    @DisplayName("@FailOnSystemExit on class - System.exit not called")
    void succeedWhenNotCallingSystemExitOnClass() {
        assertTestSucceeds(FailOnSystemExitAtClassLevelWithoutSystemExitTest.class);
    }

    @SuppressWarnings("JUnitMalformedDeclaration")
    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    static class FailOnSystemExitAtTestLevelTest {
        @Test
        @FailOnSystemExit
        void callsSystemExit() {
            System.exit(42);
        }

        @Test
        @FailOnSystemExit
        void doesNotCallSystemExit() {
            // Nothing to do
        }
    }

    @SuppressWarnings("JUnitMalformedDeclaration")
    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @FailOnSystemExit
    static class FailOnSystemExitAtClassLevelTest {
        @Test
        void callsSystemExit() {
            System.exit(42);
        }
    }

    @SuppressWarnings("JUnitMalformedDeclaration")
    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @FailOnSystemExit
    static class FailOnSystemExitAtClassLevelWithoutSystemExitTest {
        @Test
        void doesNotCallSystemExit() {
            // Nothing to do
        }
    }


}
