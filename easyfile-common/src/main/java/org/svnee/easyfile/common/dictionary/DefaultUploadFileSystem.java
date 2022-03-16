package org.svnee.easyfile.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author svnee
 * @desc
 **/
@Getter
@AllArgsConstructor
public enum DefaultUploadFileSystem implements UploadFileSystem {

    NONE("NONE", "无"),

    ;

    private final String systemCode;
    private final String desc;
}
