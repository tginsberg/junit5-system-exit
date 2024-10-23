# JUnit5 System.exit() Extension

This [JUnit 5 Extension](https://junit.org/junit5/docs/current/user-guide/#extensions) helps you write tests for code that calls `System.exit()`. It requires Java 17 or greater,
and has one dependency ([ASM](https://asm.ow2.io/)).

## Differences Between Version 1.x and 2.x

**Version 1.x** used an approach that [replaced the system `SecurityManager`](https://todd.ginsberg.com/post/testing-system-exit/).
That worked fine until Java 17 where the `SecurityManager` was deprecated for removal. As of Java 18, a property to 
explicitly enable programmatic access to the `SecurityManager` is required. This method works for now but will eventually 
stop working. If you are still on 1.x and cannot move to 2.x, [you can still find the instructions here](Version1.xInstructions.md).

**Version 2.x** uses a Java Agent to rewrite bytecode as the JVM loads classes. Whenever a call to `System.exit()` is detected, 
the Junit 5 System Exit Agent replaces that call with a function that records an attempt to exit, preventing the JVM from exiting. 
As a consequence of rewriting bytecode, this library now has one dependency - [ASM](https://asm.ow2.io/). 
When the [Java Class-File API](https://openjdk.org/jeps/457) is released, I will explore using that instead (or in addition to).

Version 2 also supports AssertJ-style fluid assertions in addition to the annotation-driven approach that came with Version 1. 
Other than enabling the Java Agent (see below), your code should not change when upgrading from Version 1.x to Version 2.x.

## Installing

Installing involves two steps: adding the `junit5-system-exit` library to your build, and adding its Java Agent to 
your test task. Please consult the [FAQ](#faq) below if you run into problems.

### Gradle

#### 1. Copy the following into your `build.gradle` or `build.gradle.kts`.

```groovy
testImplementation("com.ginsberg:junit5-system-exit:2.0.1")
```

#### 2. Enable the Java Agent

It is important to add the Junit5 System Exit Java Agent in a way that makes it come after other agents which
you may be using, such as JaCoCo. See the notes below for details on why.

If you use the Groovy DSL, add this code to your `build.gradle` file in the `test` task...

```groovy
// Groovy DSL

test {
    useJUnitPlatform()

    def junit5SystemExit = configurations.testRuntimeClasspath.files
            .find { it.name.contains('junit5-system-exit') }
    jvmArgumentProviders.add({["-javaagent:$junit5SystemExit"]} as CommandLineArgumentProvider)
}
```

or if you use the Kotlin DSL...

```kotlin
// Kotlin DSL
test {
    useJUnitPlatform()

    jvmArgumentProviders.add(CommandLineArgumentProvider {
        listOf("-javaagent:${configurations.testRuntimeClasspath.get().files.find { 
            it.name.contains("junit5-system-exit") }
        }")
    })
}
```

### Maven

#### 1. Copy the following into your `pom.xml`

```xml
<dependency>
    <groupId>com.ginsberg</groupId>
    <artifactId>junit5-system-exit</artifactId>
    <version>2.0.1</version>
    <scope>test</scope>
</dependency>
```

#### 2. Enable the Java Agent

In `pom.xml`, add the `properties` goal to `maven-dependency-plugin`, in order to create properties for each dependency.

```xml
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Then add the following `<argLine/>` to `maven-surefire-plugin`, which references the property we just created for 
this library. This should account for other Java Agents and run after any others you may have, such as JaCoCo.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>@{argLine} -javaagent:${com.ginsberg:junit5-system-exit:jar}</argLine>
    </configuration>
</plugin>
```

And 

## Use Cases - Annotation-based

**A Test that expects `System.exit()` to be called, with any status code:**

```java
public class MyTestCases { 
    
    @Test
    @ExpectSystemExit
    void thatSystemExitIsCalled() {
        System.exit(1);
    }
}
```

**A Test that expects `System.exit(1)` to be called, with a specific status code:**

```java
public class MyTestCases {
    
    @Test
    @ExpectSystemExitWithStatus(1)
    void thatSystemExitIsCalled() {
        System.exit(1);
    }
}
```

**A Test that should not expect `System.exit()` to be called, and fails the test if it does:**

```java
public class MyTestCases {
    
    @Test
    @FailOnSystemExit
    void thisTestWillFail() {
        System.exit(1); // !!!
    }
}
```

The `@ExpectSystemExit`, `@ExpectSystemExitWithStatus`, and `@FailOnSystemExit` annotations can be applied to methods, classes, or annotations (to act as meta-annotations).

## Use Cases - Assertion-based

**A Test that expects `System.exit()` to be called, with any status code:**

```java
public class MyTestClasses {
    
    @Test
    void thatSystemExitIsCalled() {
        assertThatCallsSystemExit(() -> 
                System.exit(42)
        );
    }
}
```

**A Test that expects `System.exit(1)` to be called, with a specific status code:**

```java
public class MyTestClasses {
    
    @Test
    void thatSystemExitIsCalled() {
        assertThatCallsSystemExit(() -> 
                System.exit(42)
        ).withExitCode(42);
    }
}
```

**A Test that expects `System.exit(1)` to be called, with status code in a specified range (inclusive):**

```java
public class MyTestClasses {
    
    @Test
    void thatSystemExitIsCalled() {
        assertThatCallsSystemExit(() -> 
                System.exit(42)
        ).withExitCodeInRange(1, 100);
    }
}
```

**A Test that should not expect `System.exit()` to be called, and fails the assertion if it does:**

```java
public class MyTestClasses {
    
    @Test
    void thisTestWillFail() {
        assertThatDoesNotCallSystemExit(() ->
                System.exit(42) // !!!
        );
    }
}
```

## FAQ

### :question: I don't want `Junit5-System-Exit` to rewrite the bytecode of a specific class or method that calls `System.exit()`.

This is supported. When this library detects any annotation called `@DoNotRewriteExitCalls` on any method or class, bytecode 
rewriting will be skipped. While this library ships with its own implementation of `@DoNotRewriteExitCalls`, you'll probably
want to write your own so you don't have to use this library outside the test scope. It is a marker annotation like this:

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DoNotRewriteExitCalls {
}
```

### :question: JaCoCo issues a warning - "Execution data for class <some class> does not match"

This happens when JaCoCo's Java Agent runs after this one. The instructions above _should_ put this agent after JaCoCo
but things change over time, and there are probably more gradle configurations that I have not tested. If you run into
this and are confident that you followed the instructions above, please reach out to me with a minimal example and I will
see what I can do.

### :question: JaCoCo coverage is not accurate

Because this Java Agent rewrites bytecode and must run after JaCoCo, there will be discrepancies in the JaCoCo Report.
I have not found a way to account for this, but if you discover something please let me know!

## Contributing and Issues

Please feel free to file issues for change requests or bugs. If you would like to contribute new functionality, please contact me first!

Copyright &copy; 2021-2024 by Todd Ginsberg
