package pl.techblock.core.downloader.common;

public class KlocConfig {

    private String Version;
    private int ModPackID;

    public KlocConfig(){}

    public void setVersion(String version) {
        this.Version = version;
    }

    public void setModPackID(int modPackID) {
        ModPackID = modPackID;
    }

    public String getVersion() {
        return Version;
    }

    public int getModPackID() {
        return ModPackID;
    }
}