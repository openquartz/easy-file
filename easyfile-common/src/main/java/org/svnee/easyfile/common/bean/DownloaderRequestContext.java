package org.svnee.easyfile.common.bean;

import java.io.OutputStream;

/**
 * 请求上下文
 *
 * @author svnee
 */
public class DownloaderRequestContext extends BaseDownloaderRequestContext {

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
}
