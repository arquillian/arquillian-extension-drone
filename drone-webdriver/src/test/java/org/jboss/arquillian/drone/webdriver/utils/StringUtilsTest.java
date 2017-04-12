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
package org.jboss.arquillian.drone.webdriver.utils;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for StringUtils tokenizer
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 */
public class StringUtilsTest {

    @Test
    public void tokenizeEmptyString() {
        List<String> list = StringUtils.tokenize("");
        Assert.assertEquals("List is empty", 0, list.size());
    }

    @Test
    public void tokenizeNull() {
        List<String> list = StringUtils.tokenize(null);
        Assert.assertEquals("List is empty", 0, list.size());
    }

    @Test
    public void tokenizeSimpleTokens() {
        List<String> list = StringUtils.tokenize("foo bar");
        Assert.assertEquals("List is not empty", 2, list.size());
        Assert.assertEquals("Expecting foo", "foo", list.get(0));
        Assert.assertEquals("Expecting bar", "bar", list.get(1));
    }

    @Test
    public void tokenizeQuotedTokens() {
        List<String> list = StringUtils.tokenize("\"foo bar\" baz");
        Assert.assertEquals("List is not empty", 2, list.size());
        Assert.assertEquals("Expecting foo bar", "foo bar", list.get(0));
        Assert.assertEquals("Expecting baz", "baz", list.get(1));
    }

    @Test
    public void tokenizeQuotedNonClosedTokens() {
        List<String> list = StringUtils.tokenize("\"foo bar baz");
        Assert.assertEquals("List is not empty", 3, list.size());
        Assert.assertEquals("Expecting \"foo", "\"foo", list.get(0));
        Assert.assertEquals("Expecting bar", "bar", list.get(1));
        Assert.assertEquals("Expecting baz", "baz", list.get(2));
    }

    @Test
    public void tokenizeQuotedNonClosedTokens2() {
        List<String> list = StringUtils.tokenize("\"foo bar\" baz\"");
        Assert.assertEquals("List is not empty", 2, list.size());
        Assert.assertEquals("Expecting foo bar", "foo bar", list.get(0));
        Assert.assertEquals("Expecting baz\"", "baz\"", list.get(1));
    }

    @Test
    public void tokenizeQuotedNonClosedTokens3() {
        List<String> list = StringUtils.tokenize("\"foo bar\" baz \"");
        Assert.assertEquals("List is not empty", 3, list.size());
        Assert.assertEquals("Expecting foo bar", "foo bar", list.get(0));
        Assert.assertEquals("Expecting bar", "baz", list.get(1));
        Assert.assertEquals("Expecting \"", "\"", list.get(2));
    }
}
