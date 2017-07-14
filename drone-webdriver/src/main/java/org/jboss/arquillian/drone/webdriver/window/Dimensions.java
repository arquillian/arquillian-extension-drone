package org.jboss.arquillian.drone.webdriver.window;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;

public class Dimensions {

    public static final Pattern DIMENSIONS_PATTERN = Pattern.compile("([0-9]+)x([0-9]+)");

    private String dimensions;
    private int width;
    private int height;

    public Dimensions(WebDriverConfiguration configuration) {
        this.dimensions = getDimensions(configuration);
        if (dimensions != null) {
            setDimensions(dimensions);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isFullscreenSet() {
        if (dimensions != null) {
            if (dimensions.equals("full") || dimensions.equals("fullscreen") || dimensions.equals("max")) {
                return true;
            }
        }
        return false;
    }

    public boolean areDimensionsPositive() {
        return width > 0 && height > 0;
    }

    private String getDimensions(WebDriverConfiguration configuration) {
        String dimensions = null;
        if (configuration.getDimensions() != null) {
            dimensions = configuration.getDimensions().toLowerCase().trim();
        }
        return dimensions;
    }

    private void setDimensions(String dimensions) {
        Matcher m = DIMENSIONS_PATTERN.matcher(dimensions);
        if (m.matches()) {
            width = Integer.valueOf(m.group(1));
            height = Integer.valueOf(m.group(2));
        }
    }
}



