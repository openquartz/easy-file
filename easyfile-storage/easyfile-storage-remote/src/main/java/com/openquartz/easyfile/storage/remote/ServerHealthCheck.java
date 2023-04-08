package com.openquartz.easyfile.storage.remote;

/**
 * Server health check.
 *
 * @author svnee
 */
public interface ServerHealthCheck {

    /**
     * Is health status.
     *
     * @return healthStatus
     */
    boolean isHealthStatus();

    /**
     * Set health status.
     *
     * @param healthStatus healthStatus
     */
    void setHealthStatus(boolean healthStatus);

}
