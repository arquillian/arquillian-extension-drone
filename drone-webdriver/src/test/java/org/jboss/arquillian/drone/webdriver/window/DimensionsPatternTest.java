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

import java.util.regex.Matcher;
import org.junit.Assert;
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
