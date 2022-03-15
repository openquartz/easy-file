package org.svnee.easyfile.common.dictionary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.svnee.easyfile.common.constants.Constants;

/**
 * 文件后缀枚举
 *
 * @author xuzhao
 */
@Getter
@AllArgsConstructor
public enum FileSuffixEnum {

    EXCEL_07("xlsx", "2007版excel"),
    EXCEL_03("xls", "2003版excel"),
    CSV("csv", "CSV"),
    PDF("pdf", "PDF"),
    ZIP("zip", "ZIP压缩文件"),
    ;

    private final String code;

    private final String desc;

    /**
     * 获取文件后缀 (含分隔符)
     *
     * @return 文件后缀
     */
    public final String getFullFileSuffix() {
        return Constants.FILE_SUFFIX_SEPARATOR + this.getCode();
    }
}
