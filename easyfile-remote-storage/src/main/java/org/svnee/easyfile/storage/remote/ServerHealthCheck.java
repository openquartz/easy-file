package org.svnee.easyfile.storage.remote;

/**
 * Server health check.
 *
 * @author svnee
 * @date 2021/12/8 20:08
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
