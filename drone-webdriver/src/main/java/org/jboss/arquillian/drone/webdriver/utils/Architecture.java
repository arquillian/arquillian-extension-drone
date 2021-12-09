package org.jboss.arquillian.drone.webdriver.utils;

public enum Architecture {
    BIT64("64"),
    BIT32("32"),
    AUTO_DETECT(PlatformUtils.is64() ? BIT64.getValue() : BIT32.getValue());

    private final String value;

    Architecture(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
