/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.arquillian.selenium;

import java.util.Arrays;
import java.util.Collection;

import org.jboss.arquillian.impl.bootstrap.ConfigurationRegistrar;
import org.jboss.arquillian.impl.bootstrap.ContextActivator;
import org.jboss.arquillian.impl.bootstrap.ContextDeActivator;
import org.jboss.arquillian.impl.bootstrap.ServiceLoaderRegistrar;
import org.jboss.arquillian.impl.client.container.ContainerCreator;
import org.jboss.arquillian.impl.client.container.ContainerDeployer;
import org.jboss.arquillian.impl.client.container.ContainerRegistryCreator;
import org.jboss.arquillian.impl.client.container.ContainerStarter;
import org.jboss.arquillian.impl.client.container.ContainerStopper;
import org.jboss.arquillian.impl.client.container.ContainerUndeployer;
import org.jboss.arquillian.impl.client.deployment.ArchiveDeploymentExporter;
import org.jboss.arquillian.impl.client.deployment.DeploymentGenerator;
import org.jboss.arquillian.impl.client.protocol.ProtocolRegistryCreator;
import org.jboss.arquillian.impl.client.protocol.RemoteTestExecuter;
import org.jboss.arquillian.impl.handler.TestCaseEnricher;
import org.jboss.arquillian.impl.handler.TestEventExecuter;
import org.jboss.arquillian.selenium.impl.ContextPathInjector;
import org.jboss.arquillian.selenium.impl.SeleniumConfigurator;
import org.jboss.arquillian.selenium.impl.SeleniumCreator;
import org.jboss.arquillian.selenium.impl.SeleniumDestroyer;
import org.jboss.arquillian.selenium.impl.SeleniumInjector;
import org.jboss.arquillian.selenium.impl.SeleniumServerCreator;
import org.jboss.arquillian.selenium.impl.SeleniumServerDestroyer;
import org.jboss.arquillian.spi.Profile;

/**
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumProfile implements Profile
{
   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.Profile#getClientProfile()
    */
   @SuppressWarnings("unchecked")
   public Collection<Class<?>> getClientProfile()
   {
      return Arrays.asList
            (
                  // core
                  ContextActivator.class,
                  ServiceLoaderRegistrar.class,
                  ConfigurationRegistrar.class,
                  ProtocolRegistryCreator.class,
                  ContainerRegistryCreator.class,

                  // container / deploy / test
                  ContainerCreator.class,
                  ContainerStarter.class,
                  DeploymentGenerator.class,
                  ContainerDeployer.class,
                  ContainerUndeployer.class,
                  TestCaseEnricher.class,
                  RemoteTestExecuter.class,
                  ContainerStopper.class,

                  // utils
                  ArchiveDeploymentExporter.class,

                  // selenium extension
                  SeleniumConfigurator.class,
                  SeleniumServerCreator.class,
                  SeleniumServerDestroyer.class,

                  SeleniumCreator.class,
                  SeleniumInjector.class,
                  SeleniumDestroyer.class,

                  ContextPathInjector.class,

                  // core
                  ContextDeActivator.class
            );
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.jboss.arquillian.spi.Profile#getContainerProfile()
    */
   @SuppressWarnings("unchecked")
   public Collection<Class<?>> getContainerProfile()
   {
      return Arrays.asList
            (
                  // core
                  ContextActivator.class,
                  ServiceLoaderRegistrar.class,
                  // ConfigurationRegistrar.class,

                  // container / deploy / test
                  TestCaseEnricher.class,
                  TestEventExecuter.class,

                  // core
                  ContextDeActivator.class
            );
   }

}
