package com.openquartz.easyfile.common.bean;

/**
 * Interface for DTOs that can hold an error message during import.
 *
 * @author svnee
 */
public interface ImportErrorMsgAware {

    /**
     * Set the error message.
     *
     * @param errorMsg error message
     */
    void setErrorMsg(String errorMsg);

    /**
     * Get the error message.
     *
     * @return error message
     */
    String getErrorMsg();
}
