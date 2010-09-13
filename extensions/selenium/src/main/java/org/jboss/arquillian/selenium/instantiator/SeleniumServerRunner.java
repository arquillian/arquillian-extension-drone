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
package org.jboss.arquillian.selenium.instantiator;

import static org.jboss.arquillian.selenium.instantiator.SeleniumConstants.DEFAULT_SERVER_PORT;
import static org.jboss.arquillian.selenium.instantiator.SeleniumConstants.DEFAULT_TIMEOUT;
import static org.jboss.arquillian.selenium.instantiator.SeleniumConstants.SERVER_PORT_KEY;
import static org.jboss.arquillian.selenium.instantiator.SeleniumConstants.TIMEOUT_KEY;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jboss.arquillian.selenium.meta.ArquillianConfiguration;
import org.jboss.arquillian.selenium.meta.Configuration;
import org.jboss.arquillian.selenium.meta.OverridableConfiguration;
import org.jboss.arquillian.selenium.meta.SystemPropertiesConfiguration;

/**
 * SeleniumServerRunner allows to run and kill Selenium server, if present on
 * actual Java classpath.
 * 
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumServerRunner
{
   public static final String SERVER_ENABLE_KEY = "arquillian.selenium.server.enable";

   public static final String SERVER_OUTPUT_KEY = "arquillian.selenium.server.output";

   public static final String SERVER_CMDLINE_KEY = "arquillian.selenium.server.cmdline";

   public static final String SERVER_CLASSNAME_KEY = "arquillian.selenium.server.classname";

   private static final String DEFAULT_ENABLE = "false";
   private static final String DEFAULT_OUTPUT = "target/selenium-server-output.log";
   private static final String DEFAULT_CMDLINE = "";
   private static final String DEFAULT_CLASSNAME = "org.openqa.selenium.server.SeleniumServer";

   // check Selenium server output to determine if it is started already
   private static final String SERVER_TOKEN = "Started org.openqa.jetty.jetty.Server";

   private Configuration configuration;

   private JavaRunner server;
   private OutputConsumerThread consumer;

   private boolean enabled;

   public SeleniumServerRunner()
   {
      this.configuration = new OverridableConfiguration(new ArquillianConfiguration(), new SystemPropertiesConfiguration());

      this.enabled = Boolean.parseBoolean(configuration.getString(SERVER_ENABLE_KEY, DEFAULT_ENABLE));
   }

   /**
    * Starts Selenium server if enabled in configuration
    * 
    * @throws IOException If Selenium server was not present on path or java
    *            binary was not found
    */
   public void start() throws IOException
   {
      if (enabled)
      {
         int port = configuration.getInt(SERVER_PORT_KEY, DEFAULT_SERVER_PORT);

         String output = configuration.getString(SERVER_OUTPUT_KEY, DEFAULT_OUTPUT);
         String cmdLine = configuration.getString(SERVER_CMDLINE_KEY, DEFAULT_CMDLINE);
         String className = configuration.getString(SERVER_CLASSNAME_KEY, DEFAULT_CLASSNAME);
         // timeout in seconds
         int timeout = configuration.getInt(TIMEOUT_KEY, Integer.parseInt(DEFAULT_TIMEOUT)) / 1000;

         String classPath = SecurityActions.getProperty("java.class.path");

         List<String> args = new ArrayList<String>();
         args.add("-port");
         args.add(String.valueOf(port));

         // parse command line arguments
         StringTokenizer parser = new StringTokenizer(cmdLine, " ");
         while (parser.hasMoreTokens())
            args.add(parser.nextToken());

         this.server = new JavaRunner().spawn(className, classPath, args);

         OutputStream os = new FileOutputStream(new File(output));

         this.consumer = new OutputConsumerThread(server.getProcessOutput(), os, SERVER_TOKEN);

         consumer.setDaemon(false);
         consumer.start();

         // actively wait until Selenium server is started
         boolean started = false;
         for (int i = 0; i < timeout; i++)
         {
            if (consumer.isTokenFound())
            {
               started = true;
               break;
            }

            try
            {
               Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
               throw new RuntimeException("The thread was interrupted while waiting for Selenium server startup", e);
            }
         }

         if (!started)
         {
            throw new RuntimeException("The Selenium server was not started during time limit of " + timeout + " seconds.");
         }

      }
   }

   /**
    * Stops Selenium server if it was enabled in the configuration
    */
   public void stop()
   {
      if (enabled)
      {
         // killing server with flush consumer input
         server.kill();
         try
         {
            consumer.join();
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException("The flushing thread was interrupted before finished");
         }
      }
   }

   /**
    * Consumer of an input thread, which flushes input to the outpet.
    * 
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   private class OutputConsumerThread extends Thread
   {

      private BufferedReader input;
      private BufferedWriter output;

      private String token;

      private boolean tokenFound;

      public OutputConsumerThread(InputStream input, OutputStream output, String token)
      {
         this.input = new BufferedReader(new InputStreamReader(input));
         this.output = new BufferedWriter(new OutputStreamWriter(output));
         this.token = token;
         this.tokenFound = false;

      }

      /*
       * (non-Javadoc)
       * 
       * @see java.lang.Thread#run()
       */
      public void run()
      {
         String nl = SecurityActions.getProperty("line.separator");
         try
         {
            String line = null;
            while ((line = input.readLine()) != null)
            {
               if (line.contains(token))
               {
                  setTokenFound(true);
               }

               output.write(line);
               output.write(nl);
               output.flush();
            }
         }
         catch (IOException e)
         {
            // ignore exception, because we are killing subprocess in a nasty
            // way
         }
         try
         {
            input.close();
            output.close();
         }
         catch (IOException e)
         {
            // ignore exception, because we are killing subprocess in a nasty
            // way
         }

      }

      /**
       * Checks if token was already found in the input
       * 
       * @return {@code true} if token was found, {@code false} otherwise
       */
      public synchronized boolean isTokenFound()
      {
         return tokenFound;
      }

      /**
       * Sets whether token was already found in the input
       * 
       * @param tokenFound the flag whether the token was found
       */
      public synchronized void setTokenFound(boolean tokenFound)
      {
         this.tokenFound = tokenFound;
      }
   }

   /**
    * JavaRunner provides more convenient way how to run a Java class as a
    * standalone process
    * 
    * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
    * 
    */
   private class JavaRunner
   {
      private Process process;

      public JavaRunner spawn(String className, String classPath, List<String> args) throws IOException
      {
         String javaHome = SecurityActions.getProperty("java.home");
         String javaBin = new StringBuilder(javaHome).append(File.separator).append("bin").append(File.separator).append("java").toString();

         List<String> runArgs = new ArrayList<String>();
         runArgs.add(javaBin);
         runArgs.add("-cp");
         runArgs.add(classPath);
         runArgs.add(className);
         runArgs.addAll(args);

         process = SecurityActions.spawnProcess(runArgs);
         return this;
      }

      public void kill()
      {
         System.out.println("KILLING: " + process.toString());
         process.destroy();
      }

      public InputStream getProcessOutput()
      {
         if (process != null)
         {
            return process.getInputStream();
         }
         throw new RuntimeException("Unable to get process output, it was not spawned yet.");
      }
   }

}
