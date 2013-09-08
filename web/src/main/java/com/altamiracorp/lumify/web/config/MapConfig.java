package com.altamiracorp.lumify.web.config;

/**
 * Interface for accessing map component specific configuration values
 */
public interface MapConfig {
    public String getMapProvider();
    public String getMapAccessKey();
    public String getMapTileServerHostname();
    public int getMapTileServerPort();
}
