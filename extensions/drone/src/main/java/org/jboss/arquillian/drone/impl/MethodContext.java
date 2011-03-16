package org.jboss.arquillian.drone.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holder of Drone context for method based life cycle. It is able to store different instances of drone
 * instances as well as their configurations and to retrieve them during
 * testing.
 * 
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class MethodContext
{
   private ConcurrentHashMap<Method, DroneContext> cache = new ConcurrentHashMap<Method, DroneContext>();

   /**
    * Gets context with is bound to a test method
    * @param key The test method
    * @return Drone context
    */
   public DroneContext get(Method key)
   {
      return cache.get(key);
   }

   /**
    * Puts value into context if it doesn't exist already
    * @param key The test method
    * @param value Context for method
    * @return Actual context for method
    */
   public DroneContext getOrCreate(Method key)
   {
      DroneContext newContext = new DroneContext();      
      DroneContext dc = cache.putIfAbsent(key, new DroneContext());
      return dc==null ? newContext : dc;
   }

   /**
    * Removes context bound to a method
    * @param key The test method
    * @return Modified instance
    */
   public MethodContext remove(Method key)
   {
      cache.remove(key);
      return this;
   }
}
