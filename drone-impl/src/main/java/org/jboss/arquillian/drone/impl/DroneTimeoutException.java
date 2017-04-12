package org.jboss.arquillian.drone.impl;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

/**
 * Representation of timeout related exception in Drone
 */
public class DroneTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 6727104529033908184L;

    public DroneTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public DroneTimeoutException(int timeoutInSeconds, Throwable cause) {
        this(timeoutInSeconds, cause, null);
    }

    public DroneTimeoutException(int timeoutInSeconds, Throwable cause, String messageFormat, Object... params) {
        super(constructMessage(timeoutInSeconds, messageFormat, params), cause);
    }

    /**
     * Checks whether any cause in hierarchy is a TimeoutException. TimeoutException is determited by class ending by
     * TimeoutException string.
     */
    public static boolean isCausedByTimeoutException(Throwable throwable) {
        Throwable reason = throwable;
        while (reason != null) {
            if (reason.getClass().getName().endsWith("TimeoutException")) {
                return true;
            }
            reason = reason.getCause();
        }
        return false;
    }

    private static String constructMessage(int timeoutInSeconds, String messageFormat, Object[] params) {

        StringBuilder msg = new StringBuilder();

        msg.append("Drone creation request timed out after ")
            .append(timeoutInSeconds)
            .append(" ")
            .append(TimeUnit.SECONDS.toString().toLowerCase())
            .append(
                ". Make sure that browser or remote server in case of remotely executed driver is running and communication with Drone haven't failed.");
        if (messageFormat != null) {
            msg.append("\n").append(MessageFormat.format(messageFormat, params));
        }

        return msg.toString();
    }
}
