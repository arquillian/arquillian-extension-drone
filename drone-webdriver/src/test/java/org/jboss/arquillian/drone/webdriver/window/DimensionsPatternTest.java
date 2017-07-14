/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.drone.webdriver.window;

import org.jboss.arquillian.drone.webdriver.configuration.WebDriverConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class DimensionsPatternTest {

    @Test
    public void validSample() {
        WebDriverConfiguration mockedConfiguration = getMockedConfiguration("100x200");
        Dimensions dimensions = new Dimensions(mockedConfiguration);
        Assert.assertEquals(100, dimensions.getWidth());
        Assert.assertEquals(200, dimensions.getHeight());
    }

    @Test
    public void invalidSample() {
        WebDriverConfiguration mockedConfiguration = getMockedConfiguration("100,200");
        Dimensions dimensions = new Dimensions(mockedConfiguration);
        Assert.assertEquals(0, dimensions.getWidth());
        Assert.assertEquals(0, dimensions.getHeight());
    }

    @Test
    public void validSampleWithFullscreen() {
        WebDriverConfiguration mockedConfiguration = getMockedConfiguration("fullscreen");
        Dimensions dimensions = new Dimensions(mockedConfiguration);
        Assert.assertEquals(0, dimensions.getWidth());
        Assert.assertEquals(0, dimensions.getHeight());
    }

    private WebDriverConfiguration getMockedConfiguration(String dimension) {
        WebDriverConfiguration configuration = Mockito.mock(WebDriverConfiguration.class);
        when(configuration.getDimensions()).thenReturn(dimension);
        return configuration;
    }
}
