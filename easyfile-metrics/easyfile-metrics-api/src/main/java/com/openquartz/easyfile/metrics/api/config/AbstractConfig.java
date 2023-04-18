package com.openquartz.easyfile.metrics.api.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The type Abstract config.
 *
 * @author svnee
 */
public abstract class AbstractConfig {

    private volatile boolean isLoad;

    /**
     * Whether to passively receive push.
     */
    private boolean passive;

    /**
     * source map.
     */
    private Map<String, Object> source = new HashMap<>();

    public void flagLoad() {
        isLoad = true;
    }

    public boolean isLoad() {
        return isLoad;
    }

    public boolean isPassive() {
        return passive;
    }

    /**
     * Sets passive.
     *
     * @param passive the passive
     */
    public void setPassive(final boolean passive) {
        this.passive = passive;
    }

    /**
     * Sets load.
     *
     * @param load the load
     */
    public void setLoad(final boolean load) {
        isLoad = load;
    }

    public void setSource(final Map<String, Object> source) {
        this.source = source;
    }

    public Map<String, Object> getSource() {
        return source;
    }
}
