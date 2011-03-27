/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.drone.mockdrone;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.Instantiator;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class MockDroneFactory implements Configurator<MockDroneInstance, MockDroneConfiguration>, Instantiator<MockDroneInstance, MockDroneConfiguration>, Destructor<MockDroneInstance>
{
   public static final String FIELD_OVERRIDE = "System property @Different";

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.Sortable#getPrecedence()
    */
   public int getPrecedence()
   {
      return 0;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.Configurator#createConfiguration(org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
    */
   public MockDroneConfiguration createConfiguration(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
   {
      System.setProperty("arquillian.mockdrone.different.field", FIELD_OVERRIDE);
      
      return new MockDroneConfiguration().configure(descriptor, qualifier);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.Destructor#destroyInstance(java.lang.Object)
    */
   public void destroyInstance(MockDroneInstance instance)
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.Instantiator#createInstance(org.jboss.arquillian.drone.spi.DroneConfiguration)
    */
   public MockDroneInstance createInstance(MockDroneConfiguration configuration)
   {
      MockDroneInstance instance = new MockDroneInstance(configuration.getField());
      return instance;
   }

}