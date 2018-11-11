package com.ginsberg.junit.exit;

/**
 * A marker exception so we know that a System.exit was intercepted by our
 * SecurityManager.
 */
class SystemExitPreventedException extends SecurityException {

}
