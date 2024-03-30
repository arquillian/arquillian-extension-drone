package org.jboss.arquillian.drone.webdriver.utils;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

public class ChromeUtilsTest {

    @Test
    public void nullValue() {
        //Default is Chrome for Testing
        assertThat(ChromeUtils.isChromeForTesting(null), CoreMatchers.is(true));
    }

    @Test
    public void empty() {
        //Default is Chrome for Testing
        assertThat(ChromeUtils.isChromeForTesting(""), CoreMatchers.is(true));
    }

    @Test
    public void lowerVersion() {
        assertThat(ChromeUtils.isChromeForTesting("114."), CoreMatchers.is(false));
    }

    @Test
    public void exactVersion() {
        assertThat(ChromeUtils.isChromeForTesting("115."), CoreMatchers.is(true));
    }

    @Test
    public void badVersion() {
        try {
            assertThat(ChromeUtils.isChromeForTesting("xsdkjsdkj"), CoreMatchers.is(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Cannot parse"));
        }
    }

    @Test
    public void badVersionNumber() {
        try {
            assertThat(ChromeUtils.isChromeForTesting("115"), CoreMatchers.is(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), containsString("Cannot parse"));
        }
    }
}
