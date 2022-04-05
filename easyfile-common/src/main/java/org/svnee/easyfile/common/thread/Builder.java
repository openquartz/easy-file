package org.svnee.easyfile.common.thread;

import java.io.Serializable;

/**
 * Builder pattern interface definition.
 *
 * @author svnee
 */
public interface Builder<T> extends Serializable {

    /**
     * Build.
     *
     * @return T
     */
    T build();

}
