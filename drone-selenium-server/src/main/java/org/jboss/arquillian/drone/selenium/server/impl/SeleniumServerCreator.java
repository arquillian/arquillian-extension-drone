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
package org.jboss.arquillian.drone.selenium.server.impl;

import java.io.File;
import java.io.IOException;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerConfigured;
import org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStarted;
import org.jboss.arquillian.test.spi.annotation.SuiteScoped;
import org.openqa.selenium.server.RemoteControlConfiguration;
import org.openqa.selenium.server.SeleniumServer;

/**
 * Creator of Selenium Server instance
 *
 * <p>
 * Consumes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.drone.selenium.server.configuration.SeleniumServerConfiguration}</li>
 * </ol>
 *
 * <p>
 * Produces:
 * </p>
 * <ol>
 * <li>{@link org.openqa.selenium.server.SeleniumServer}</li>
 * </ol>
 *
 * <p>
 * Fires:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.drone.selenium.server.event.SeleniumServerStarted}</li>
 * </ol>
 *
 * <p>
 * Observes:
 * </p>
 * <ol>
 * <li>{@link org.jboss.arquillian.drone.selenium.server.event.SeleniumServerConfigured}</li>
 * </ol>
 *
 * @author <a href="kpiwko@redhat.com>Karel Piwko</a>
 *
 */
public class SeleniumServerCreator {

    @Inject
    private Instance<SeleniumServerConfiguration> seleniumServerConfiguration;

    @Inject
    private Event<SeleniumServerStarted> afterStart;

    @Inject
    @SuiteScoped
    private InstanceProducer<SeleniumServer> seleniumServer;

    public void seleniumServerStartUp(@Observes SeleniumServerConfigured event) throws IOException {

        SeleniumServerConfiguration configuration = seleniumServerConfiguration.get();

        if (configuration == null || configuration.isSkip()) {
            return;
        }

        try {
            SeleniumServer server = new SeleniumServer(configure(configuration));
            SystemEnvHolder sysEnv = new SystemEnvHolder();
            sysEnv.modifyEnvBy(configuration);
            server.start();
            sysEnv.restore();

            seleniumServer.set(server);
            afterStart.fire(new SeleniumServerStarted());
        } catch (Exception e) {
            throw new RuntimeException("Unable to start Selenium Server", e);
        }
    }

    private RemoteControlConfiguration configure(SeleniumServerConfiguration configuration) {
        RemoteControlConfiguration rcc = new RemoteControlConfiguration();

        rcc.setAvoidProxy(configuration.isAvoidProxy());
        rcc.setBrowserSideLogEnabled(configuration.isBrowserSideLog());
        rcc.setDebugMode(configuration.isDebug());
        rcc.setDontTouchLogging(configuration.isDontTouchLogging());
        SecurityActions.setProperty("selenium.loglevel", configuration.isDebug() ? "DEBUG" : "INFO");

        rcc.setEnsureCleanSession(configuration.isEnsureCleanSession());

        // set firefox profile
        String ffProfile = configuration.getFirefoxProfileTemplate();
        if (Validate.isNotNullOrEmpty(ffProfile)) {
            Validate.isValidFile(ffProfile, "Firefox profile must point to a readable directory: " + ffProfile);
            rcc.setFirefoxProfileTemplate(new File(ffProfile));
        }

        // force browser type
        String forcedMode = configuration.getForcedBrowserMode();
        if (Validate.isNotNullOrEmpty(forcedMode)) {
            rcc.setForcedBrowserMode(forcedMode);
        }

        rcc.setHonorSystemProxy(configuration.isHonorSystemProxy());

        // log file
        String logFile = configuration.getLogFile();
        if (Validate.isNotNullOrEmpty(logFile)) {
            Validate.isInReadableDirectory(logFile, "Log file cannot be created: " + logFile);
            rcc.setLogOutFile(new File(logFile));
        }

        rcc.setPort(configuration.getPort());

        // set profile location
        String profiles = configuration.getProfilesLocation();
        if (Validate.isNotNullOrEmpty(profiles)) {
            Validate.isValidFile(profiles, "Profiles location must point to a readable directory: " + profiles);
            rcc.setProfilesLocation(new File(profiles));
        }

        rcc.setProxyInjectionModeArg(configuration.isProxyInjectionMode());
        rcc.setReuseBrowserSessions(configuration.isBrowserSessionReuse());
        rcc.setRetryTimeoutInSeconds(configuration.getRetryTimeoutInSeconds());
        rcc.setSingleWindow(configuration.isSingleWindow());
        rcc.setTimeoutInSeconds(configuration.getTimeoutInSeconds());
        rcc.setTrustAllSSLCertificates(configuration.isTrustAllSSLCertificates());

        String userExtensions = configuration.getUserExtensions();
        if (Validate.isNotNullOrEmpty(userExtensions)) {
            Validate.isValidFile(userExtensions, "User extensions must point to a valid file");
            rcc.setUserExtensions(new File(userExtensions));
        }

        return rcc;
    }
}