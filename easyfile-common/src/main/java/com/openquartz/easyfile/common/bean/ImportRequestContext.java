package com.openquartz.easyfile.common.bean;

import java.io.InputStream;
import java.util.Objects;

/**
 * 导入请求上下文
 * @author svnee
 */
public final class ImportRequestContext extends BaseImportRequestContext {

    /**
     * 输入流
     * 必须
     */
    private InputStream in;

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) {
        this.in = in;
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
        ImportRequestContext that = (ImportRequestContext) o;
        return in.equals(that.in);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), in);
    }
}
