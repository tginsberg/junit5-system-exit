/*
 * MIT License
 *
 * Copyright (c) 2018 Todd Ginsberg
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.ginsberg.junit.exit.TestUtils.assertTestFails;

class ExpectSystemExitTest {

    @Nested
    @DisplayName("Success Cases")
    class HappyPath {
        @Test
        @DisplayName("System.exit() is caught and detected")
        @ExpectSystemExit
        void detectSystemExit() {
            System.exit(1234);
        }

        @Test
        @DisplayName("System.exit() is caught and detected within a thread")
        @ExpectSystemExit
        void detectSystemExitInThread() throws InterruptedException {
            final Thread t = new Thread(() -> System.exit(1234));
            t.start();
            t.join();
        }

        @Nested
        @DisplayName("Class Success")
        @ExpectSystemExit
        class ExpectedSuccessClassLevel {
            @Test
            @DisplayName("Method in class annotated with ExpectSystemExit succeeds")
            void classLevelExpect() {
                System.exit(123456);
            }
        }
    }

    @Nested
    @DisplayName("Failure Cases")
    class FailurePath {
        @Test
        @DisplayName("System.exit() is expected for method but not called")
        void expectSystemExitThatDoesNotHappenMethod() {
            assertTestFails(ExpectedFailuresAtTestLevel.class, "doNotCallSystemExit");
        }

        @Test
        @DisplayName("System.exit() is expected for class but not called")
        void expectSystemExitThatDoesNotHappenClass() {
            assertTestFails(ExpectedFailuresAtClassLevel.class);
        }
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    static class ExpectedFailuresAtTestLevel {
        @Test
        @ExpectSystemExit
        void doNotCallSystemExit() {
            // Done! :)
        }
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @ExpectSystemExit
    static class ExpectedFailuresAtClassLevel {
        @Test
        void doNotCallSystemExit() {
            // Done! :)
        }
    }

}
