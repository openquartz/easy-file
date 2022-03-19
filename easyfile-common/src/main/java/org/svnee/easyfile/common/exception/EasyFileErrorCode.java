package org.svnee.easyfile.common.exception;

/**
 * 异常Code
 *
 * @author svnee
 **/
public interface EasyFileErrorCode {

    /**
     * 异常Code
     *
     * @return 异常code码
     */
    String getErrorCode();

    /**
     * 异常信息
     *
     * @return 异常信息描述
     */
    String getErrorMsg();

}
