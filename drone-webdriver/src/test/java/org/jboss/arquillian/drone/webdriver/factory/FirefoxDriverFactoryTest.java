package org.jboss.arquillian.drone.webdriver.factory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.firefox.FirefoxDriver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.arquillian.drone.webdriver.utils.ArqDescPropertyUtil.assumeBrowserEqual;

@RunWith(Arquillian.class)
public class FirefoxDriverFactoryTest {

    @Drone
    private FirefoxDriver driver;

    @BeforeClass
    public static void checkIfFirefoxIsSet(){
        assumeBrowserEqual("firefox");
    }

    @Test
    public void should_contain_user_preferences_set() throws IOException {
        // given
        // used prefs.js in arquillian.xml with predefined user preferences

        // when
        // loaded FirefoxDriver with FirefoxOptions containing the user preferences

        //then
        List<String> prefsFile = Files.readAllLines(Paths.get("src", "test", "resources", "prefs.js"));
        List<String> expLines = prefsFile.stream()
            .filter(line -> line.startsWith("user_pref"))
            .collect(Collectors.toList());
        Path userPrefFile = Paths.get((String) driver.getCapabilities().getCapability("moz:profile"), "user.js");
        assertThat(Files.readAllLines(userPrefFile)).containsAll(expLines);
    }
}
