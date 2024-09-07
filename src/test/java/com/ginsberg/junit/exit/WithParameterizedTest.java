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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.ginsberg.junit.exit.TestUtils.assertParameterizedTestFails;

public class WithParameterizedTest {

    @Test
    @DisplayName("@ParameterizedTest on method should reset state between tests")
    void failOnSystemExitOnClass() {
        assertParameterizedTestFails(WithParameterizedTest.SucceedsAndThenFailsTest.class, true, false, true);
    }

    @EnabledIfSystemProperty(named = "running_within_test", matches = "true")
    static class SucceedsAndThenFailsTest {
        @ParameterizedTest(name = "{index}")
        @ValueSource(booleans = {true, false, true})
        @ExpectSystemExit
        public void testBasicAssumptions(boolean shouldSucceed) {
            if(shouldSucceed) {
                System.exit(1); // This test expects a system exit to succeed
            }
        }
    }
}
