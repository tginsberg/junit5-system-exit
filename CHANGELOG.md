# Change Log for `junit5-system-exit`

### 2.0.1
- Bugfix: [[#20]](https://github.com/tginsberg/junit5-system-exit/issues/20): Multiple calls to `System.exit()` do not always report the first exit status code.

### 2.0.0
- Remove terminally deprecated `SecurityManager` approach for preventing `System.exit()` calls.
- Add Java Agent-based approach. Calls to `System.exit()` are rewritten as classes are loaded.
- Add AssertJ-style fluid assertions for cases when test authors do not want to use annotations, or want to write assertions after a `System.exit()` is detected.

### 1.1.2
- Bugfix: [[#12]](https://github.com/tginsberg/junit5-system-exit/issues/12) ParameterizedTest does not work accurately.

### 1.1.1
- Make `SystemExitPreventedException` public and add a `statusCode` getter. This should help with testing CLIs.
- Add new `@FailOnSystemExit` annotation. When a test calls `System.exit()` the JVM running the test will terminate (in most setups). Annotating a test with `@FailOnSystemExit` will catch this condition and fail the test, rather than exiting the JVM the test is executing on.

### 1.1.0
- Do Not Use. Prefer 1.1.1 or 1.1.2 please.

### 1.0.0
- Initial Release.