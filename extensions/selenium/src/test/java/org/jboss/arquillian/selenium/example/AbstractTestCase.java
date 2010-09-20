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
package org.jboss.arquillian.selenium.example;

import java.io.File;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.selenium.example.webapp.Credentials;
import org.jboss.arquillian.selenium.example.webapp.LoggedIn;
import org.jboss.arquillian.selenium.example.webapp.Login;
import org.jboss.arquillian.selenium.example.webapp.User;
import org.jboss.arquillian.selenium.example.webapp.Users;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * This class shares deployment method for all available tests.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public abstract class AbstractTestCase
{

   /**
    * Creates a WAR of a Weld based application using ShrinkWrap
    * 
    * @return WebArchive to be tested
    */
   @Deployment
   public static WebArchive createDeployment()
   {
      WebArchive war = ShrinkWrap.create(WebArchive.class, "weld-login.war")
            .addClasses(Credentials.class, LoggedIn.class, Login.class, User.class, Users.class)
            .addWebResource(new File("src/test/webapp/WEB-INF/beans.xml"), "beans.xml")
            .addWebResource(new File("src/test/webapp/WEB-INF/faces-config.xml"), "faces-config.xml")
            .addWebResource(new File("src/test/resources/import.sql"), ArchivePaths.create("classes/import.sql"))
            .addResource(new File("src/test/webapp/index.html"), ArchivePaths.create("index.html"))
            .addResource(new File("src/test/webapp/home.xhtml"), ArchivePaths.create("home.xhtml"))
            .addResource(new File("src/test/webapp/template.xhtml"), ArchivePaths.create("template.xhtml"))
            .addResource(new File("src/test/webapp/users.xhtml"), ArchivePaths.create("users.xhtml"))
            .addManifestResource(new File("src/test/resources/META-INF/persistence.xml"))
            .setWebXML(new File("src/test/webapp/WEB-INF/web.xml"));

      return war;
   }

}
