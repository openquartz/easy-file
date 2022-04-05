package org.svnee.easyfile.common.thread;

import java.io.Serializable;

/**
 * Builder pattern interface definition.
 *
 * @author svnee
 * @date 2021/7/5 21:39
 */
public interface Builder<T> extends Serializable {

    /**
     * Build.
     *
     * @return
     */
    T build();

}
