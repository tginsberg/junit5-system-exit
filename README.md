# JUnit5 System.exit() Extension

This [JUnit 5 Extension](https://junit.org/junit5/docs/current/user-guide/#extensions) helps you write tests for code 
that calls `System.exit()`. Starting with JUnit 5, @Rules, @ClassRules, and Runners were replaced by the Extension concept.

## Installing

Copy the following into your `build.gradle` or `build.xml`.

**Gradle**

```groovy
testImplementation("com.ginsberg:junit5-system-exit:1.1.2")
```

**Maven**

```xml
<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>junit5-system-exit</artifactId>
    <version>1.1.2</version>
    <scope>test</scope>
</dependency>
```

## Notes On Compatibility With Newer JVMs

Starting with **Java 17**, the use of the `SecurityManager` class ([see details of its use](https://todd.ginsberg.com/post/testing-system-exit/)) is deprecated 
and will emit a warning. [This is a known issue](https://github.com/tginsberg/junit5-system-exit/issues/10) and once an alternative is available, I will 
explore the possibility of upgrading this library.

Starting with **Java 18**, the default behavior of the JVM was changed from allowing the `SecurityManager` to be changed at runtime to disallowing it by default.
To re-enable the pre-Java 18 behavior, set the system property `java.security.manager` to the string `allow`. 
The details [can be read here](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/SecurityManager.html#set-security-manager) if you are interested.

So, for Gradle:

```groovy
test {
    systemProperty 'java.security.manager', 'allow'
}
```

And for Maven:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <systemPropertyVariables>
            <java.security.manager>allow</java.security.manager>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

**Why doesn't `junit5-system-exit` set this property for me?** 

Good question, it certainly could! However, given the decision to terminally deprecate the `SecurityManager`, overriding this decision in a way that is not completely
obvious to the people using is not something I want to do. I feel that users should make a conscious positive decision to override this behavior and take the time 
to evaluate the needs of their project. I don't want somebody caught by surprise that the `SecurityManager` is behaving in a non-default way because they happen to be
using a testing library.

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

Copyright &copy; 2021-2024 by Todd Ginsberg
