package com.ginsberg.junit.exit;

import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

class TestUtils {

    static void assertTestFails(final Class clazz, final String testMethod) {
        final SummaryGeneratingListener listener = executeTest(clazz, testMethod);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsFailedCount(), "Single test should have failed");
    }

    static void assertTestFails(final Class clazz) {
        final SummaryGeneratingListener listener = executeTest(clazz, null);

        assertEquals(1, listener.getSummary().getTestsFoundCount(), "Should have found one test");
        assertEquals(1, listener.getSummary().getTestsFailedCount(), "Single test should have failed");
    }

    /**
     * Execute the given test and then return a summary of its execution. This is used for tests that
     * succeed when other tests fail ("Test that a test decorated with X fails when...")
     */
    private static SummaryGeneratingListener executeTest(final Class clazz, final String testMethod) {
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
