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

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.lang.annotation.Annotation;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

/**
 * Does the work of installing the DisallowExitSecurityManager, interpreting the test results, and
 * returning the original SecurityManager to service.
 */
public class SystemExitExtension implements BeforeEachCallback, AfterEachCallback, TestExecutionExceptionHandler {
    private Integer expectedStatusCode;
    private boolean failOnSystemExit;
    private final DisallowExitSecurityManager disallowExitSecurityManager = new DisallowExitSecurityManager(System.getSecurityManager());
    private SecurityManager originalSecurityManager;

    @Override
    public void afterEach(ExtensionContext context) {
        // Return the original SecurityManager, if any, to service.
        System.setSecurityManager(originalSecurityManager);

        if (disallowExitSecurityManager.getFirstExitStatusCode() != null && failOnSystemExit) {
            fail("Unexpected System.exit() with status code '" +
                    disallowExitSecurityManager.getFirstExitStatusCode() +
                    "' caught");
        } else if (expectedStatusCode == null) {
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

        // Should we fail on a System.exit() rather than letting it bubble out?
        failOnSystemExit = getAnnotation(context, FailOnSystemExit.class).isPresent();

        // Get the expected exit status code, if any
        getAnnotation(context, ExpectSystemExitWithStatus.class).ifPresent(code -> expectedStatusCode = code.value());

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
    private <T extends Annotation> Optional<T> getAnnotation(final ExtensionContext context, final Class<T> annotationClass) {
        final Optional<T> method = findAnnotation(context.getTestMethod(), annotationClass);
        if (method.isPresent()) {
            return method;
        } else {
            return findAnnotation(context.getTestClass(), annotationClass);
        }
    }
}
