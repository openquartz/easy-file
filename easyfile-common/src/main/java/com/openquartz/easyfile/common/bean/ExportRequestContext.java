package com.openquartz.easyfile.common.bean;

import lombok.Getter;
import lombok.Setter;

import java.io.OutputStream;
import java.util.Objects;

/**
 * 请求上下文
 *
 * @author svnee
 */
@Setter
@Getter
public final class ExportRequestContext extends BaseExportRequestContext {

    /**
     * 输出流对象
     * 必须
     */
    private OutputStream out;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        ExportRequestContext that = (ExportRequestContext) o;
        return Objects.equals(out, that.out);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), out);
    }
}
