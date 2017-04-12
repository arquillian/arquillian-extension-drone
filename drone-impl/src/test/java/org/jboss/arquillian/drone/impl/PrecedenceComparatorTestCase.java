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
package org.jboss.arquillian.drone.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.jboss.arquillian.drone.spi.Sortable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PrecedenceComparatorTestCase {

    private final Sortable sortable1 = new Sortable() {
        @Override
        public int getPrecedence() {
            return 400;
        }

        @Override
        public String toString() {
            return "1";
        }
    };

    private final Sortable sortable2 = new Sortable() {
        @Override
        public int getPrecedence() {
            return 200;
        }

        @Override
        public String toString() {
            return "2";
        }
    };

    private final Sortable sortable3 = new Sortable() {
        @Override
        public int getPrecedence() {
            return 0;
        }

        @Override
        public String toString() {
            return "3";
        }
    };

    @Test
    public void testPrecedenceOrder() {
        // having
        List<Sortable> expected = createList(sortable1, sortable2, sortable3);
        List<Sortable> actual = createList(sortable3, sortable1, sortable2);

        // when
        Collections.sort(actual, PrecedenceComparator.getInstance());

        // then
        assertEquals(expected, actual);
    }

    @Test
    public void testReversedOrder() {
        // having
        List<Sortable> expected = createList(sortable3, sortable2, sortable1);
        List<Sortable> actual = createList(sortable3, sortable1, sortable2);

        // when
        Collections.sort(actual, PrecedenceComparator.getReversedOrder());

        // then
        assertEquals(expected, actual);
    }

    private List<Sortable> createList(Sortable... sortables) {
        return new ArrayList<Sortable>(Arrays.asList(sortables));
    }
}
