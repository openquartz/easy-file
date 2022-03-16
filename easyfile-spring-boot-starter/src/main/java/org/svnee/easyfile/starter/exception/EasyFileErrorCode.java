package org.svnee.easyfile.starter.exception;

/**
 * @author svnee
 * @desc 异常Code
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
