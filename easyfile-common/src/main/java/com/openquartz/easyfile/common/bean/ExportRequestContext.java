package com.openquartz.easyfile.common.bean;

import java.io.OutputStream;
import java.util.Objects;

/**
 * 请求上下文
 *
 * @author svnee
 */
public final class ExportRequestContext extends BaseExportRequestContext {

    /**
     * 输出流对象
     * 必须
     */
    private OutputStream out;

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

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
