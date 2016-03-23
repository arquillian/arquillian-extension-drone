package org.arquillian.drone.saucelabs.extension.connect;

/**
 * Throws when something bad happens during running SauceConnect binary
 *
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class SauceConnectException extends RuntimeException {

    public SauceConnectException() {
        super();
    }

    public SauceConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    public SauceConnectException(String message) {
        super(message);
    }

    public SauceConnectException(Throwable cause) {
        super(cause);
    }
}