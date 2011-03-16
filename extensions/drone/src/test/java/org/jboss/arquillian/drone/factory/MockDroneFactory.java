/**
 * 
 */
package org.jboss.arquillian.drone.factory;

import java.lang.annotation.Annotation;

import org.jboss.arquillian.drone.configuration.ConfigurationMapper;
import org.jboss.arquillian.drone.spi.Configurator;
import org.jboss.arquillian.drone.spi.Destructor;
import org.jboss.arquillian.drone.spi.DroneConfiguration;
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

class MockDroneInstance
{
   private String field;

   public MockDroneInstance(String field)
   {
      this.setField(field);
   }

   /**
    * @param field the field to set
    */
   public void setField(String field)
   {
      this.field = field;
   }

   /**
    * @return the field
    */
   public String getField()
   {
      return field;
   }
}

class MockDroneConfiguration implements DroneConfiguration<MockDroneConfiguration>
{

   private String field;

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.DroneConfiguration#getConfigurationName()
    */
   public String getConfigurationName()
   {
      return "mockdrone";
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.drone.spi.DroneConfiguration#configure(org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor, java.lang.Class)
    */
   public MockDroneConfiguration configure(ArquillianDescriptor descriptor, Class<? extends Annotation> qualifier)
   {
      ConfigurationMapper.fromArquillianDescriptor(descriptor, this, qualifier);
      return ConfigurationMapper.fromSystemConfiguration(this, qualifier);
   }

   /**
    * @param field the field to set
    */
   public void setField(String field)
   {
      this.field = field;
   }

   /**
    * @return the field
    */
   public String getField()
   {
      return field;
   }

}