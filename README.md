# JUnit5 System.exit() Extension

This [JUnit 5 Extension](https://junit.org/junit5/docs/current/user-guide/#extensions) helps you write tests for code 
that calls `System.exit()`. Starting with JUnit 5, @Rules, @ClassRules, and Runners were replaced by the Extension concept.

## Installing

Copy the following into your `build.gradle` or `build.xml`.

**Gradle**

```groovy
testImplementation("com.ginsberg:junit5-system-exit:1.0.0")
```

**Maven**

```xml
<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>junit5-system-exit</artifactId>
    <version>1.0.0</version>
    <scope>test</scope>
</dependency>
```


## Use cases

**A Test that expects `System.exit()` to be called, with any status code:**

```java
public class MyTestCases { 
    
    @Test
    @ExpectSystemExit
    public void thatSystemExitIsCalled() {
        System.exit(1);
    }
}
```

**A Test that expects `System.exit(1)` to be called, with a specific status code:**

```java
public class MyTestCases {
    
    @Test
    @ExpectSystemExitWithStatus(1)
    public void thatSystemExitIsCalled() {
        System.exit(1);
    }
}
```

**A Test that should not expect `System.exit(1)` to be called, and fails the test if it does:**

```java
public class MyTestCases {
    
    @Test
    @FailOnSystemExit
    public void thisTestWillFail() {
        System.exit(1);
    }
}
```

The `@ExpectSystemExit`, `@ExpectSystemExitWithStatus`, and `@FailOnSystemExit` annotations can be applied to methods, classes, or annotations (to act as meta-annotations).

## Contributing and Issues

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please contact me first!

Copyright &copy; 2021 by Todd Ginsberg
