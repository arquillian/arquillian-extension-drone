package org.jboss.arquillian.drone.webdriver.binary.downloading;

/**
 * A representation of some external binary. Each external binary is represented by it's version and by a url that
 * points to the binary.
 */
public class ExternalBinary {

    private String version;
    private String url;

    public ExternalBinary(String version, String url) {
        this.version = version;
        this.url = url;
    }

    public ExternalBinary(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ExternalBinary)) {
            return false;
        }

        final ExternalBinary that = (ExternalBinary) o;

        if (getVersion() != null ? !getVersion().equals(that.getVersion()) : that.getVersion() != null) {
            return false;
        }
        return getUrl() != null ? getUrl().equals(that.getUrl()) : that.getUrl() == null;
    }

    @Override
    public int hashCode() {
        int result = getVersion() != null ? getVersion().hashCode() : 0;
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        return result;
    }
}
