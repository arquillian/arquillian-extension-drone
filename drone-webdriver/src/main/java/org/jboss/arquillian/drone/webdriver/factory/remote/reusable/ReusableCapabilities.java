package org.jboss.arquillian.drone.webdriver.factory.remote.reusable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

class ReusableCapabilities implements Serializable {

    private Map<String, Object> capabilities;

    ReusableCapabilities(){
        capabilities = new HashMap<>();
    }

    void setCapability(String key, Object value) {
        capabilities.put(key, value);
    }

    Map<String, Object> getCapabilities() {
        return capabilities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ReusableCapabilities that = (ReusableCapabilities) o;

        if (getCapabilities() != null ? !getCapabilities().equals(that.getCapabilities())
            : that.getCapabilities() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return getCapabilities() != null ? getCapabilities().hashCode() : 0;
    }
}
