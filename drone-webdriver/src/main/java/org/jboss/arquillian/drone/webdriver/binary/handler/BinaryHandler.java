package org.jboss.arquillian.drone.webdriver.binary.handler;

import java.io.File;

/**
 * A representation of some binary handler. This interface is tightly connected with the abstract class
 * {@link AbstractBinaryHandler}
 */
public interface BinaryHandler {

    String checkAndSetBinary(boolean performExecutableValidations);

    File downloadAndPrepare() throws Exception;

    String getSystemBinaryProperty();
}
