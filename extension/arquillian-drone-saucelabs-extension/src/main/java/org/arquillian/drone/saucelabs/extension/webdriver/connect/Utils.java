package org.arquillian.drone.saucelabs.extension.webdriver.connect;

/**
 * @author <a href="mailto:mjobanek@redhat.com">Matous Jobanek</a>
 */
public class Utils {
    public static boolean isEmpty(String object) {
        return object == null || object.isEmpty();
    }
}
