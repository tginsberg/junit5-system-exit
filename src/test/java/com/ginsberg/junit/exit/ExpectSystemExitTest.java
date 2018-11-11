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
