package org.jboss.arquillian.drone.webdriver.binary.handler;

import org.junit.Assert;
import org.junit.Test;

import static org.jboss.arquillian.drone.webdriver.binary.handler.ChromeDriverBinaryHandler.ChromeVersion;

public class ChromeVersionTest {
    private static final ChromeVersion TEST_VERSION = new ChromeVersion(10, 20, 30, 40);

    @Test
    public void testParseVersion() {
        ChromeVersion parsed = new ChromeVersion("111.0.5563.19");

        Assert.assertEquals("Parsed major", 111, parsed.major);
        Assert.assertEquals("Parsed minor", 0, parsed.minor);
        Assert.assertEquals("Parsed patch", 5563, parsed.patch);
        Assert.assertEquals("Parsed build", 19, parsed.build);
    }

    @Test
    public void testParseLegacyVersion() {
        ChromeVersion parsed = new ChromeVersion("2.22");

        Assert.assertEquals("Parsed major", 2, parsed.major);
        Assert.assertEquals("Parsed minor", 22, parsed.minor);
        Assert.assertEquals("Parsed patch", 0, parsed.patch);
        Assert.assertEquals("Parsed build", 0, parsed.build);
    }

    @Test
    public void testIsAfterSameVersion() {
        Assert.assertFalse("isAfter same version", TEST_VERSION.isAfter(TEST_VERSION));
    }

    @Test
    public void testIsAfterNewerMajor() {
        ChromeVersion version = new ChromeVersion(11, 20, 30, 40);
        Assert.assertFalse(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterOlderMajor() {
        ChromeVersion version = new ChromeVersion(9, 20, 30, 40);
        Assert.assertTrue(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterNewerMinor() {
        ChromeVersion version = new ChromeVersion(10, 21, 30, 40);
        Assert.assertFalse(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterOlderMinor() {
        ChromeVersion version = new ChromeVersion(10, 19, 30, 40);
        Assert.assertTrue(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterNewerPatch() {
        ChromeVersion version = new ChromeVersion(10, 20, 31, 40);
        Assert.assertFalse(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterOlderPatch() {
        ChromeVersion version = new ChromeVersion(10, 20, 29, 40);
        Assert.assertTrue(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterNewerBuild() {
        ChromeVersion version = new ChromeVersion(10, 20, 30, 41);
        Assert.assertFalse(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsAfterOlderBuild() {
        ChromeVersion version = new ChromeVersion(10, 20, 30, 39);
        Assert.assertTrue(TEST_VERSION.isAfter(version));
    }

    @Test
    public void testIsBeforeSameVersion() {
        Assert.assertFalse("isBefore same version", TEST_VERSION.isBefore(TEST_VERSION));
    }

    @Test
    public void testIsBeforeNewerMajor() {
        ChromeVersion version = new ChromeVersion(11, 20, 30, 40);
        Assert.assertTrue(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeOlderMajor() {
        ChromeVersion version = new ChromeVersion(9, 20, 30, 40);
        Assert.assertFalse(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeNewerMinor() {
        ChromeVersion version = new ChromeVersion(10, 21, 30, 40);
        Assert.assertTrue(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeOlderMinor() {
        ChromeVersion version = new ChromeVersion(10, 19, 30, 40);
        Assert.assertFalse(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeNewerPatch() {
        ChromeVersion version = new ChromeVersion(10, 20, 31, 40);
        Assert.assertTrue(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeOlderPatch() {
        ChromeVersion version = new ChromeVersion(10, 20, 29, 40);
        Assert.assertFalse(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeNewerBuild() {
        ChromeVersion version = new ChromeVersion(10, 20, 30, 41);
        Assert.assertTrue(TEST_VERSION.isBefore(version));
    }

    @Test
    public void testIsBeforeOlderBuild() {
        ChromeVersion version = new ChromeVersion(10, 20, 30, 39);
        Assert.assertFalse(TEST_VERSION.isBefore(version));
    }
}
