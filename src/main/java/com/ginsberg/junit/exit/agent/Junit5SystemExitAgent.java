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
package com.ginsberg.junit.exit.agent;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Set;
import java.util.logging.Logger;

public class Junit5SystemExitAgent {

    private final static Logger log = Logger.getLogger(Junit5SystemExitAgent.class.getName());

    private Junit5SystemExitAgent() {

    }

    private final static Set<String> disallowedClassPrefixes = Set.of(
            "com/sun/", "java/", "jdk/", "worker/org/gradle/", "sun/"
    );

    private final static String SKIP_ANNOTATION = "/DoNotRewriteExitCalls;";

    public static void premain(final String agentArgs, final Instrumentation inst) {
        AgentSystemExitHandlerStrategy.agentInit();
        inst.addTransformer(new SystemExitClassTransformer());
    }

    static class SystemExitClassTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(final ClassLoader loader,
                                final String className,
                                final Class<?> classBeingRedefined,
                                final ProtectionDomain protectionDomain,
                                final byte[] classFileBuffer) {
            if (disallowedClassPrefixes.stream().anyMatch(className::startsWith)) {
                return null;
            }
            final ClassReader classReader = new ClassReader(classFileBuffer);
            final ClassWriter classWriter = new ClassWriter(classReader, 0);
            classReader.accept(new SystemExitClassVisitor(className, classWriter), 0);
            return classWriter.toByteArray();
        }
    }

    static class SystemExitClassVisitor extends ClassVisitor {
        private final String className;

        public SystemExitClassVisitor(final String className, final ClassVisitor cv) {
            super(Opcodes.ASM9, cv);
            this.className = className;
        }

        private boolean hasSkipAnnotation = false;

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            if(descriptor.endsWith(SKIP_ANNOTATION)) {
                hasSkipAnnotation = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public MethodVisitor visitMethod(final int access,
                                         final String name,
                                         final String descriptor,
                                         final String signature,
                                         final String[] exceptions) {
            if(hasSkipAnnotation) {
                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }
            return new SystemExitMethodVisitor(
                    className,
                    name,
                    super.visitMethod(access, name, descriptor, signature, exceptions)
            );
        }
    }

    static class SystemExitMethodVisitor extends MethodVisitor {
        private boolean hasSkipAnnotation = false;
        private final String className;
        private final String methodName;

        public SystemExitMethodVisitor(final String className, final String methodName, final MethodVisitor mv) {
            super(Opcodes.ASM9, mv);
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if(descriptor.endsWith(SKIP_ANNOTATION)) {
                hasSkipAnnotation = true;
            }
            return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public void visitMethodInsn(final int opcode,
                                    final String owner,
                                    final String name,
                                    final String descriptor,
                                    final boolean isInterface) {
            if (!hasSkipAnnotation && owner.equals("java/lang/System") && name.equals("exit")) {
                log.fine("Replacing System.exit() call in: " + className + "." + methodName);
                super.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/ginsberg/junit/exit/agent/AgentSystemExitHandlerStrategy",
                        "handleExit",
                        descriptor,
                        false
                );
            } else {
                if(hasSkipAnnotation) {
                    log.fine("Not replacing System.exit() call in: " + className + "." + methodName + " due to presence of 'skip this' annotation");
                }
                super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            }
        }
    }
}
