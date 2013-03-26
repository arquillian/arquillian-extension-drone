package org.jboss.arquillian.drone.impl;

import java.util.Collections;
import java.util.Comparator;

import org.jboss.arquillian.drone.spi.Sortable;

class PrecedenceComparator implements Comparator<Sortable> {

    private static final PrecedenceComparator INSTANCE = new PrecedenceComparator();

    private PrecedenceComparator() {
    }

    public static Comparator<Sortable> getInstance() {
        return INSTANCE;
    }

    public static Comparator<Sortable> getReversedOrder() {
        return Collections.reverseOrder(INSTANCE);
    }

    public int compare(Sortable o1, Sortable o2) {
        return new Integer(o2.getPrecedence()).compareTo(new Integer(o1.getPrecedence()));
    }
}