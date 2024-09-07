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

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

class TestUtils {

    static void assertTestFails(final Class<?> clazz, final String testMethod) {
        final SummaryGeneratingListener listener = executeTest(clazz, testMethod);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsFailedCount(), "Single test should have failed");
    }

    static void assertTestFails(final Class<?> clazz) {
        final SummaryGeneratingListener listener = executeTest(clazz, null);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsFailedCount(), "Single test should have failed");
    }

    static void assertTestFailsExceptionally(
            final Class<?> clazz,
            final String testMethod,
            final Class<? extends Exception> expectedExceptionClass
    ) {
        final SummaryGeneratingListener listener = executeTest(clazz, testMethod);
        assertEquals(1, listener.getSummary().getFailures().size(), "Test did not finish exceptionally");
        assertEquals(
                expectedExceptionClass,
                listener.getSummary().getFailures().get(0).getException().getClass(),
                "Failed exceptionally but not because of the expected exception"
        );

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsFailedCount(), "Single test should have failed");
    }

    static void assertParameterizedTestFails(final Class<?> clazz, final Boolean... expectedSuccess) {
        final SummaryGeneratingListener listener = executeTest(clazz, null);
        final Set<Integer> failedTestNumbers = new HashSet<>();
        for(final TestExecutionSummary.Failure failure : listener.getSummary().getFailures()) {
            failedTestNumbers.add(
                    Integer.parseInt(failure.getTestIdentifier().getDisplayName()) -1
            );
        }

        assertEquals(expectedSuccess.length, listener.getSummary().getTestsFoundCount(), "Wrong number of tests found");
        for(int testNumber = 0; testNumber < expectedSuccess.length; testNumber++) {
            if(expectedSuccess[testNumber] && failedTestNumbers.contains(testNumber)) {
                fail(String.format("Test %d should have succeeded, but didn't", testNumber));
            } else if(!expectedSuccess[testNumber] && !failedTestNumbers.contains(testNumber)) {
                fail(String.format("Test %d should have failed, but didn't", testNumber));
            }
        }

    }

    static void assertTestSucceeds(final Class<?> clazz, final String testMethod) {
        final SummaryGeneratingListener listener = executeTest(clazz, testMethod);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsSucceededCount(), "Single test should have succeeded");
    }

    static void assertTestSucceeds(final Class<?> clazz) {
        final SummaryGeneratingListener listener = executeTest(clazz, null);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsSucceededCount(), "Single test should have succeeded");
    }

    /**
     * Execute the given test and then return a summary of its execution. This is used for tests that
     * succeed when other tests fail ("Test that a test decorated with X fails when...")
     */
    private static SummaryGeneratingListener executeTest(final Class<?> clazz, final String testMethod) {
        final SummaryGeneratingListener listener = new SummaryGeneratingListener();
        try {
            System.setProperty("running_within_test", "true");
            final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                    .selectors(
                            testMethod == null ?
                                    selectClass(clazz) :
                                    selectMethod(clazz, testMethod)
                    )
                    .build();

            LauncherFactory.create().execute(request, listener);
            return listener;
        } finally {
            System.clearProperty("running_within_test");
        }
    }

}
