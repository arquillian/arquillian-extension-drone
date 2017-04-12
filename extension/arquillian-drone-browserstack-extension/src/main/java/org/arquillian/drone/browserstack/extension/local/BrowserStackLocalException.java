package org.arquillian.drone.browserstack.extension.local;

/**
 * Throws when something bad happens during running BrowserStackLocal binary
 */
public class BrowserStackLocalException extends RuntimeException {

    public BrowserStackLocalException() {
        super();
    }

    public BrowserStackLocalException(String message, Throwable cause) {
        super(message, cause);
    }

    public BrowserStackLocalException(String message) {
        super(message);
    }

    public BrowserStackLocalException(Throwable cause) {
        super(cause);
    }
}
