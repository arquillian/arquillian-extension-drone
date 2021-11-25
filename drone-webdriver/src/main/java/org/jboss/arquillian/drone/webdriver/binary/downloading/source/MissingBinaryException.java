package org.jboss.arquillian.drone.webdriver.binary.downloading.source;

public class MissingBinaryException extends RuntimeException {

    public MissingBinaryException(String message) {
        super(message);
    }
}
