package org.svnee.easyfile.storage.remote;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.svnee.easyfile.common.bean.ResponseResult;
import org.svnee.easyfile.common.constants.Constants;

/**
 * Server health check.
 *
 * @author svnee
 */
@Slf4j
public class HttpScheduledHealthCheck extends AbstractHealthCheck {

    private final HttpAgent httpAgent;

    public HttpScheduledHealthCheck(HttpAgent httpAgent) {
        this.httpAgent = httpAgent;
    }

    @Override
    protected boolean sendHealthCheck() {
        boolean healthStatus = false;
        try {
            ResponseResult<?> healthResponseResult = httpAgent.httpGetSimple(Constants.HEALTH_CHECK_PATH);
            if (healthResponseResult != null && Objects.equals(healthResponseResult.getData(), Constants.UP)) {
                healthStatus = true;
            }
        } catch (Exception ex) {
            log.error("Failed to periodically check the health status of the server.", ex);
        }

        return healthStatus;
    }

}
