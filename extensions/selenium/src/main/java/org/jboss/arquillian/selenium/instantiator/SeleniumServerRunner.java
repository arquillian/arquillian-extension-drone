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
import java.util.logging.Logger;

import org.jboss.arquillian.selenium.SeleniumExtensionConfiguration;

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
   private static final Logger log = Logger.getLogger(SeleniumServerRunner.class.getName());

   private SeleniumExtensionConfiguration configuration;

   private JavaRunner server;
   private OutputConsumerThread consumer;

   private boolean enabled;

   public SeleniumServerRunner(SeleniumExtensionConfiguration configuration)
   {
      this.configuration = configuration;
      this.enabled = configuration.isServerEnable();
   }

   /**
    * Starts Selenium server if enabled in configuration
    * 
    * @throws IOException If Selenium server was not present on path or java
    *         binary was not found
    */
   public void start() throws IOException
   {
      if (enabled)
      {
         log.info("Starting Selenium Server.");

         String classPath = SecurityActions.getProperty("java.class.path");

         List<String> args = new ArrayList<String>();
         args.add("-port");
         args.add(String.valueOf(configuration.getServerPort()));

         // parse command line arguments
         StringTokenizer parser = new StringTokenizer(configuration.getServerCmdline(), " ");
         while (parser.hasMoreTokens())
            args.add(parser.nextToken());

         this.server = new JavaRunner().spawn(configuration.getServerImplementation(), classPath, args);

         OutputStream os = new FileOutputStream(new File(configuration.getServerOutput()));

         this.consumer = new OutputConsumerThread(server.getProcessOutput(), os, configuration.getServerToken());

         consumer.setDaemon(false);
         consumer.start();

         // actively wait until Selenium server is started
         boolean started = false;
         for (int i = 0; i < configuration.getTimeout(); i += 1000)
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
            throw new RuntimeException("The Selenium server was not started during time limit of " + configuration.getTimeout() + " milliseconds.");
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
         log.info("Taking Selenium Server down.");
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
         log.fine("Killing Selenium server, details: " + process.toString());
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
