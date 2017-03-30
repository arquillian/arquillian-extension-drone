package org.jboss.arquillian.drone.webdriver.spi;

/**
 * <p>
 * Interface which is implemented by WebDriver's Augmenter during augmentation.
 * </p>
 * <p>
 * <p>
 * Allows to unwrap the augmented instance of WebDriver.
 * </p>
 *
 * @author Lukas Fryc
 */
public interface DroneAugmented {

    /**
     * Returns an augmented instance of WebDriver
     *
     * @return an augmented instance of WebDriver
     */
    Object getWrapped();
}