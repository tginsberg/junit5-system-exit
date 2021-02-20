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

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

/**
 * This is a SecurityManager that will prevent the system from exiting. If that happens, it will
 * record the status code that was attempted. All other actions are delegated to another SecurityManager,
 * optionally provided at construction.
 */
public class DisallowExitSecurityManager extends SecurityManager {
    private final SecurityManager delegatedSecurityManager;
    private Integer firstExitStatusCode;

    public DisallowExitSecurityManager(SecurityManager originalSecurityManager) {
        this.delegatedSecurityManager = originalSecurityManager;
    }

    /**
     * This is the one method we truly override in this class, all others are delegated.
     *
     * @param statusCode the exit status
     */
    @Override
    public void checkExit(int statusCode) {
        if(firstExitStatusCode == null) {
            this.firstExitStatusCode = statusCode;
        }
        throw new SystemExitPreventedException(statusCode);
    }

    public Integer getFirstExitStatusCode() {
        return firstExitStatusCode;
    }

    @Override
    public Object getSecurityContext() {
        return delegatedSecurityManager == null ? null : delegatedSecurityManager.getSecurityContext();
    }

    @Override
    public void checkPermission(Permission perm) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPermission(perm);
        }
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPermission(perm, context);
        }
    }

    @Override
    public void checkCreateClassLoader() {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkCreateClassLoader();
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkAccess(t);
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkAccess(g);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkExec(cmd);
        }
    }

    @Override
    public void checkLink(String lib) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkLink(lib);
        }
    }

    @Override
    public void checkRead(FileDescriptor fd) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkRead(fd);
        }
    }

    @Override
    public void checkRead(String file) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkRead(file);
        }
    }

    @Override
    public void checkRead(String file, Object context) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkRead(file, context);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkWrite(fd);
        }
    }

    @Override
    public void checkWrite(String file) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkWrite(file);
        }
    }

    @Override
    public void checkDelete(String file) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkDelete(file);
        }
    }

    @Override
    public void checkConnect(String host, int port) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkConnect(host, port);
        }
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkConnect(host, port, context);
        }
    }

    @Override
    public void checkListen(int port) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkListen(port);
        }
    }

    @Override
    public void checkAccept(String host, int port) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkAccept(host, port);
        }
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkMulticast(maddr);
        }
    }

    @Override
    public void checkPropertiesAccess() {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPropertiesAccess();
        }
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPropertyAccess(key);
        }
    }

    @Override
    public void checkPrintJobAccess() {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPrintJobAccess();
        }
    }

    @Override
    public void checkPackageAccess(String pkg) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPackageAccess(pkg);
        }
    }

    @Override
    public void checkPackageDefinition(String pkg) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkPackageDefinition(pkg);
        }
    }

    @Override
    public void checkSetFactory() {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkSetFactory();
        }
    }

    @Override
    public void checkSecurityAccess(String target) {
        if (delegatedSecurityManager != null) {
            delegatedSecurityManager.checkSecurityAccess(target);
        }
    }

    @Override
    public ThreadGroup getThreadGroup() {
        return delegatedSecurityManager == null ? null : delegatedSecurityManager.getThreadGroup();
    }
}
