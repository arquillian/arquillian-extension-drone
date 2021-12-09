/*
 * Copyright 2013 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.webdriver.utils;

public class PlatformUtils {

    private PlatformUtils() {
    }

    public static String getOS() {
        return System.getProperty("os.name").toLowerCase();
    }

    public static String getARCH() {
        return System.getProperty("os.arch").toLowerCase();
    }

    public static boolean isWindows() {
        return getOS().contains("win");
    }

    public static boolean isMac() {
        return getOS().contains("mac");
    }

    public static boolean isMacIntel() {
        return isMac() && getARCH().contains("x86_64");
    }

    public static boolean isMacAppleSilicon() {
        return isMac() && getARCH().contains("aarch64");
    }

    public static boolean isLinux() {
        return getOS().contains("linux");
    }

    public static boolean isUnix() {
        String os = getOS();
        return os.contains("nix") || os.contains("nux") || os.contains("aix");
    }

    public static boolean isSolaris() {
        return getOS().contains("sunos");
    }

    public static boolean is64() {
        return getARCH().contains("64");
    }

    public static boolean is32() {
        return !is64();
    }

    public static Platform platform() {
        return new Platform() {
            public OperatingSystem os() {
                if (isWindows()) {
                    return OperatingSystem.WINDOWS;
                } else if (isUnix()) {
                    return OperatingSystem.UNIX;
                } else if (isMac()) {
                    return OperatingSystem.MACOSX;
                } else if (isSolaris()) {
                    return OperatingSystem.SOLARIS;
                } else {
                    return OperatingSystem.UNKNOWN;
                }
            }

            public Architecture arch() {
                return is64() ? Architecture.BIT64 : Architecture.BIT32;
            }
        };
    }

    public enum OperatingSystem {
        WINDOWS,
        UNIX,
        MACOSX,
        SOLARIS,
        UNKNOWN;
    }

    public interface Platform {
        OperatingSystem os();

        Architecture arch();
    }
}
