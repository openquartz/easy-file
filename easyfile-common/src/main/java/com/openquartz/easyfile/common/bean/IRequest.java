package com.openquartz.easyfile.common.bean;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * IRequest
 *
 * @author svnee
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public interface IRequest {

}
