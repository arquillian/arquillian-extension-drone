/**
 * 
 */
package org.jboss.arquillian.selenium.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.jboss.arquillian.ajocado.browser.Browser;
import org.jboss.arquillian.ajocado.framework.AjocadoConfiguration;
import org.jboss.arquillian.impl.configuration.api.ArquillianDescriptor;

/**
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 * 
 */
public class ArquillianAjocadoConfiguration implements AjocadoConfiguration
{
   public static final String EXTENSION_QUALIFIER = "ajocado";

   public static final String PROPERTY_PREFIX = "arquillian.ajocado.";

   private URL contextRoot;

   private URL contextPath;

   private String browser = "*firefox";

   private File resourcesDirectory = new File("target/test-classes");

   private File buildDirectory = new File("target/");

   private String seleniumHost = "localhost";

   private int seleniumPort = 14444;

   private boolean seleniumMaximize = false;

   private boolean seleniumDebug = false;

   private boolean seleniumNetworkTrafficEnabled = false;

   private int seleniumSpeed = 0;

   private long seleniumTimeoutDefault = 30000;

   private long seleniumTimeoutGui = 5000;

   private long seleniumTimeoutAjax = 15000;

   private long seleniumTimeoutModel = 30000;

   /**
    * 
    */
   public ArquillianAjocadoConfiguration()
   {
      initContextPaths();
      new ConfigurationMapper(Collections.<String, String> emptyMap(), PROPERTY_PREFIX).map(this);
   }

   public ArquillianAjocadoConfiguration(ArquillianDescriptor descriptor)
   {
      initContextPaths();
      new ConfigurationMapper(descriptor, EXTENSION_QUALIFIER, PROPERTY_PREFIX).map(this);
   }

   /**
    * @return the contextRoot
    */
   public URL getContextRoot()
   {
      return contextRoot;
   }

   /**
    * @param contextRoot the contextRoot to set
    */
   public void setContextRoot(URL contextRoot)
   {
      this.contextRoot = contextRoot;
   }

   /**
    * @return the contextPath
    */
   public URL getContextPath()
   {
      return contextPath;
   }

   /**
    * @param contextPath the contextPath to set
    */
   public void setContextPath(URL contextPath)
   {
      this.contextPath = contextPath;
   }

   /**
    * @return the browser
    */
   public Browser getBrowser()
   {
      return new Browser(browser);
   }

   /**
    * @param browser the browser to set
    */
   public void setBrowser(String browser)
   {
      this.browser = browser;
   }

   /**
    * @return the resourcesDirectory
    */
   public File getResourcesDirectory()
   {
      return resourcesDirectory;
   }

   /**
    * @param resourcesDirectory the resourcesDirectory to set
    */
   public void setResourcesDirectory(File resourcesDirectory)
   {
      this.resourcesDirectory = resourcesDirectory;
   }

   /**
    * @return the buildDirectory
    */
   public File getBuildDirectory()
   {
      return buildDirectory;
   }

   /**
    * @param buildDirectory the buildDirectory to set
    */
   public void setBuildDirectory(File buildDirectory)
   {
      this.buildDirectory = buildDirectory;
   }

   /**
    * @return the seleniumHost
    */
   public String getSeleniumHost()
   {
      return seleniumHost;
   }

   /**
    * @param seleniumHost the seleniumHost to set
    */
   public void setSeleniumHost(String seleniumHost)
   {
      this.seleniumHost = seleniumHost;
   }

   /**
    * @return the seleniumPort
    */
   public int getSeleniumPort()
   {
      return seleniumPort;
   }

   /**
    * @param seleniumPort the seleniumPort to set
    */
   public void setSeleniumPort(int seleniumPort)
   {
      this.seleniumPort = seleniumPort;
   }

   /**
    * @return the seleniumMaximize
    */
   public boolean isSeleniumMaximize()
   {
      return seleniumMaximize;
   }

   /**
    * @param seleniumMaximize the seleniumMaximize to set
    */
   public void setSeleniumMaximize(boolean seleniumMaximize)
   {
      this.seleniumMaximize = seleniumMaximize;
   }

   /**
    * @return the seleniumSpeed
    */
   public int getSeleniumSpeed()
   {
      return seleniumSpeed;
   }

   /**
    * @param seleniumSpeed the seleniumSpeed to set
    */
   public void setSeleniumSpeed(int seleniumSpeed)
   {
      this.seleniumSpeed = seleniumSpeed;
   }

   /**
    * @return the seleniumNetworkTrafficEnabled
    */
   public boolean isSeleniumNetworkTrafficEnabled()
   {
      return seleniumNetworkTrafficEnabled;
   }

   /**
    * @param seleniumNetworkTrafficEnabled the seleniumNetworkTrafficEnabled to
    *           set
    */
   public void setSeleniumNetworkTrafficEnabled(boolean seleniumNetworkTrafficEnabled)
   {
      this.seleniumNetworkTrafficEnabled = seleniumNetworkTrafficEnabled;
   }

   /**
    * @return the seleniumTimeoutDefault
    */
   public long getSeleniumTimeoutDefault()
   {
      return seleniumTimeoutDefault;
   }

   /**
    * @param seleniumTimeoutDefault the seleniumTimeoutDefault to set
    */
   public void setSeleniumTimeoutDefault(long seleniumTimeoutDefault)
   {
      this.seleniumTimeoutDefault = seleniumTimeoutDefault;
   }

   /**
    * @return the seleniumTimeoutGui
    */
   public long getSeleniumTimeoutGui()
   {
      return seleniumTimeoutGui;
   }

   /**
    * @param seleniumTimeoutGui the seleniumTimeoutGui to set
    */
   public void setSeleniumTimeoutGui(long seleniumTimeoutGui)
   {
      this.seleniumTimeoutGui = seleniumTimeoutGui;
   }

   /**
    * @return the seleniumTimeoutAjax
    */
   public long getSeleniumTimeoutAjax()
   {
      return seleniumTimeoutAjax;
   }

   /**
    * @param seleniumTimeoutAjax the seleniumTimeoutAjax to set
    */
   public void setSeleniumTimeoutAjax(long seleniumTimeoutAjax)
   {
      this.seleniumTimeoutAjax = seleniumTimeoutAjax;
   }

   /**
    * @return the seleniumTimeoutModel
    */
   public long getSeleniumTimeoutModel()
   {
      return seleniumTimeoutModel;
   }

   /**
    * @param seleniumTimeoutModel the seleniumTimeoutModel to set
    */
   public void setSeleniumTimeoutModel(long seleniumTimeoutModel)
   {
      this.seleniumTimeoutModel = seleniumTimeoutModel;
   }

   /**
    * @param seleniumDebug the seleniumDebug to set
    */
   public void setSeleniumDebug(boolean seleniumDebug)
   {
      this.seleniumDebug = seleniumDebug;
   }

   /**
    * @return the seleniumDebug
    */
   public boolean isSeleniumDebug()
   {
      return seleniumDebug;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.jboss.arquillian.ajocado.framework.AjocadoConfiguration#getTimeout
    * (org.jboss.arquillian.ajocado.framework.AjocadoConfiguration.TimeoutType)
    */
   public long getTimeout(TimeoutType type)
   {
      switch (type)
      {
      case DEFAULT:
         return seleniumTimeoutDefault;
      case GUI:
         return seleniumTimeoutGui;
      case AJAX:
         return seleniumTimeoutAjax;
      case MODEL:
         return seleniumTimeoutModel;
      }

      throw new UnsupportedOperationException("Unable to determite wait time for given timout type: " + type);

   }

   private void initContextPaths()
   {
      try
      {
         this.contextRoot = new URI("http://localhost:8080").toURL();
         this.contextPath = new URI("http://localhost:8080").toURL();
      }
      catch (MalformedURLException e)
      {
         throw new IllegalStateException("Unable to set default value for contextRoot", e);
      }
      catch (URISyntaxException e)
      {
         throw new IllegalStateException("Unable to set default value for contextRoot", e);
      }

   }

}