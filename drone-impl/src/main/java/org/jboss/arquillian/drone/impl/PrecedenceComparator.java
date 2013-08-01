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

import java.util.Collections;
import java.util.Comparator;

import org.jboss.arquillian.drone.spi.Sortable;

/**
 * Comparator of {@link Sortable} interfaces
 *
 * @author Lukas Fryc
 *
 */
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

    @Override
    public int compare(Sortable o1, Sortable o2) {
        return new Integer(o2.getPrecedence()).compareTo(new Integer(o1.getPrecedence()));
    }
}