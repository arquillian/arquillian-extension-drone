
package org.arquillian.drone.saucelabs.extension.webdriver.connect;

public class PlatformUtils {

    public static String OS = System.getProperty("os.name").toLowerCase();
    public static String ARCH = System.getProperty("os.arch").toLowerCase();

    public enum OperatingSystem {
        WINDOWS, UNIX, MACOSX, SOLARIS, UNKNOWN;
    }

    public enum Architecture {
        BIT64, BIT32;
    }

    private PlatformUtils() {
    }

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isUnix() {
        return OS.contains("nix") || OS.contains("nux") || OS.contains("aix");
    }

    public static boolean isSolaris() {
        return OS.contains("sunos");
    }

    public static boolean is64() {
        return System.getProperty("os.arch").contains("64");
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

    public interface Platform {
        OperatingSystem os();

        Architecture arch();
    }

}
