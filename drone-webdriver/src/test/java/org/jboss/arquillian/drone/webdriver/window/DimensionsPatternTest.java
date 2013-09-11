package org.jboss.arquillian.drone.webdriver.window;

import java.util.regex.Matcher;

import junit.framework.Assert;

import org.junit.Test;

public class DimensionsPatternTest {

    @Test
    public void validSample() {
        Matcher m = WindowResizer.DIMENSIONS_PATTERN.matcher("100x200");
        Assert.assertTrue(m.matches());
        Assert.assertEquals("100", m.group(1));
        Assert.assertEquals("200", m.group(2));
    }

    @Test
    public void invalidSample() {
        Matcher m = WindowResizer.DIMENSIONS_PATTERN.matcher("100,200");
        Assert.assertFalse(m.matches());
    }

}
