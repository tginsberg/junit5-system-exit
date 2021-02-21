/*
 * MIT License
 *
 * Copyright (c) 2021 Todd Ginsberg
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
        assertTestFails(FailOnSystemExitAtTestLevel.class, "callsSystemExit");
    }

    @Test
    @DisplayName("@FailOnSystemExit on method - System.exit not called")
    void succeedWhenNotCallingSystemExitInMethod() {
        assertTestSucceeds(FailOnSystemExitAtTestLevel.class, "doesNotCallSystemExit");
    }

    @Test
    @DisplayName("@FailOnSystemExit on class - exception caught and fails test")
    void failOnSystemExitOnClass() {
        assertTestFails(FailOnSystemExitAtClassLevel.class);
    }

    @Test
    @DisplayName("@FailOnSystemExit on class - System.exit not called")
    void succeedWhenNotCallingSystemExitOnClass() {
        assertTestSucceeds(FailOnSystemExitAtClassLevelWithoutSystemExit.class);
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    static class FailOnSystemExitAtTestLevel {
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

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @FailOnSystemExit
    static class FailOnSystemExitAtClassLevel {
        @Test
        void callsSystemExit() {
            System.exit(42);
        }
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @FailOnSystemExit
    static class FailOnSystemExitAtClassLevelWithoutSystemExit {
        @Test
        void doesNotCallSystemExit() {
            // Nothing to do
        }
    }


}
