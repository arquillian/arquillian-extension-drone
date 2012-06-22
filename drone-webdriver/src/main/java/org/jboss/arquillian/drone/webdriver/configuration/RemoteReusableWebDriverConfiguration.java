package org.jboss.arquillian.drone.webdriver.configuration;

/**
 * Marks a browser having a reusable session in remote mode.
 *
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 *
 */
public interface RemoteReusableWebDriverConfiguration extends RemoteWebDriverConfiguration {

    boolean isRemoteReusable();

    void setRemoteReusable(boolean reusable);
}
