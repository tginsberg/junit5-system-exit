package com.ginsberg.junit.exit;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

public class SystemExitExtension implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
    private Integer expectedStatusCode;
    private final DisallowExitSecurityManager disallowExitSecurityManager = new DisallowExitSecurityManager(System.getSecurityManager());
    private SecurityManager originalSecurityManager;

    @Override
    public void afterEach(ExtensionContext context) {
        // Return the original SecurityManager, if any, to service.
        System.setSecurityManager(originalSecurityManager);
        if (expectedStatusCode == null) {
            assertNotNull(
                    disallowExitSecurityManager.getFirstExitStatusCode(),
                    "Expected System.exit() to be called, but it was not"
            );
        } else {
            assertEquals(
                    expectedStatusCode,
                    disallowExitSecurityManager.getFirstExitStatusCode(),
                    "Expected System.exit(" + expectedStatusCode + ") to be called, but it was not."
            );
        }
    }

    @Override
    public void beforeEach(final ExtensionContext context) {
        // Set aside the current SecurityManager
        originalSecurityManager = System.getSecurityManager();

        // Get the expected exit status code, if any
        getAnnotation(context).ifPresent(code -> expectedStatusCode = code.value());

        // Install our own SecurityManager
        System.setSecurityManager(disallowExitSecurityManager);
    }

    /**
     * This is here so we can catch exceptions thrown by our own security manager and prevent them from
     * stopping the annotated test. If anything other than our own exception comes through, throw it because
     * the system SecurityManager to which we delegate prevented some other action from happening.
     *
     * @param context   the current extension context; never {@code null}
     * @param throwable the {@code Throwable} to handle; never {@code null}
     * @throws Throwable if the throwable argument is not a SystemExitPreventedException
     */
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        if (!(throwable instanceof SystemExitPreventedException)) {
            throw throwable;
        }
    }

    // Find the annotation on a method, or failing that, a class.
    private Optional<ExpectSystemExitWithStatus> getAnnotation(final ExtensionContext context) {
        final Optional<ExpectSystemExitWithStatus> method = findAnnotation(context.getTestMethod(), ExpectSystemExitWithStatus.class);
        if (method.isPresent()) {
            return method;
        } else {
            return findAnnotation(context.getTestClass(), ExpectSystemExitWithStatus.class);
        }
    }
}
