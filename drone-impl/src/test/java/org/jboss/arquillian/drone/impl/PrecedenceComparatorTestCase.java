package org.jboss.arquillian.drone.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.drone.spi.Sortable;
import org.junit.Test;

public class PrecedenceComparatorTestCase {

    private Sortable sortable1 = new Sortable() {
        public int getPrecedence() {
            return 400;
        }

        public String toString() {
            return "1";
        }
    };

    private Sortable sortable2 = new Sortable() {
        public int getPrecedence() {
            return 200;
        }

        public String toString() {
            return "2";
        }
    };

    private Sortable sortable3 = new Sortable() {
        public int getPrecedence() {
            return 0;
        }

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
