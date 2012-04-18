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
package org.jboss.arquillian.drone.webdriver.configuration;

/**
 * @author <a href="mailto:jpapouse@redhat.com">Jan Papousek</a>
 */
public interface OperaDriverConfiguration extends CommonWebDriverConfiguration {

    // getters

    String getOperaArguments();

    String getOperaBinary();

    int getOperaDisplay();

    String getOperaLauncher();

    String getOperaLoggingFile();

    String getOperaLoggingLevel();

    int getOperaPort();

    String getOperaProduct();

    String getOperaProfile();

    boolean isOperaAutostart();

    boolean isOperaIdle();

    boolean isOperaQuit();

    boolean isOperaRestart();

    // setters

    void setOperaArguments(String operaArguments);

    void setOperaBinary(String operaBinary);

    void setOperaDisplay(int operaDisplay);

    void setOperaLauncher(String operaLauncher);

    void setOperaLoggingFile(String operaLoggingFile);

    void setOperaLoggingLevel(String operaLoggingLevel);

    void setOperaPort(int operaPort);

    void setOperaProduct(String operaProduct);

    void setOperaProfile(String operaProfile);

    void setOperaAutostart(boolean operaAutostart);

    void setOperaIdle(boolean operaIdle);

    void setOperaQuit(boolean operaQuit);

    void setOperaRestart(boolean operaRestart);
}
