package org.arquillian.drone.browserstack.extension.utils;

import org.apache.commons.lang3.SystemUtils;

/**
 *
 */
public class Utils {

    public static boolean isNullOrEmpty(String object) {
        return object == null || object.isEmpty();
    }

    public static boolean is64() {
        return SystemUtils.OS_ARCH.contains("64");
    }
}
