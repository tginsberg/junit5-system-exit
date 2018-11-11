package com.ginsberg.junit.exit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static com.ginsberg.junit.exit.TestUtils.assertTestFails;

class ExpectSystemExitWithStatusTest {

    @Nested
    @DisplayName("Success Cases")
    class HappyPath {
        @Test
        @DisplayName("System.exit(1234) is caught and detected")
        @ExpectSystemExitWithStatus(1234)
        void detectSystemExit() {
            System.exit(1234);
        }

        @Test
        @DisplayName("System.exit(1234) is caught and detected within a thread")
        @ExpectSystemExitWithStatus(1234)
        void detectSystemExitInThread() throws InterruptedException {
            final Thread t = new Thread(() -> System.exit(1234));
            t.start();
            t.join();
        }

        @Nested
        @DisplayName("Class Success")
        @ExpectSystemExitWithStatus(123456)
        class ExpectedSuccessClassLevel {
            @Test
            @DisplayName("Method in class annotated with ExpectSystemExitWithStatus(123456) succeeds")
            void classLevelExpect() {
                System.exit(123456);
            }
        }
    }

    @Nested
    @DisplayName("Failure Cases")
    class FailurePath {
        @Test
        @DisplayName("System.exit(1234) is expected but not called at all, on method")
        void expectSystemExitThatDoesNotHappenMethod() {
            assertTestFails(ExpectedFailuresAtMethodLevel.class, "doNotCallSystemExit");
        }

        @Test
        @DisplayName("System.exit(1234) is expected but another code was used, on method")
        void expectSystemExitWithDifferentCodeMethod() {
            assertTestFails(ExpectedFailuresAtMethodLevel.class, "exitWith4567");
        }

        @Test
        @DisplayName("System.exit(1234) is expected but not called at all, on method")
        void expectSystemExitThatDoesNotHappenClass() {
            assertTestFails(ExpectedFailuresAtClassLevelNoExit.class);
        }

        @Test
        @DisplayName("System.exit(1234) is expected but another code was used, on class")
        void expectSystemExitWithDifferentCodeClass() {
            assertTestFails(ExpectedFailuresAtClassLevelDifferentCode.class);
        }
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    static class ExpectedFailuresAtMethodLevel {

        @Test
        @ExpectSystemExitWithStatus(1234)
        void doNotCallSystemExit() {
            // Done! :)
        }

        @Test
        @ExpectSystemExitWithStatus(1234)
        void exitWith4567() {
            System.exit(4567);
        }
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @ExpectSystemExitWithStatus(1234)
    static class ExpectedFailuresAtClassLevelNoExit {

        @Test
        void doNotCallSystemExit() {
            // Done! :)
        }

    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    @ExpectSystemExitWithStatus(1234)
    static class ExpectedFailuresAtClassLevelDifferentCode {
        @Test
        void exitWith4567() {
            System.exit(4567);
        }
    }

}
