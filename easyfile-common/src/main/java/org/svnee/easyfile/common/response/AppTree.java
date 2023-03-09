package org.svnee.easyfile.common.response;

import java.util.List;
import lombok.Data;

/**
 * AppTree
 *
 * @author svnee
 **/
@Data
public class AppTree {

    /**
     * appId
     */
    private String appId;

    /**
     * child app
     */
    private List<String> childList;

    public AppTree() {
    }

    public AppTree(String appId, List<String> childList) {
        this.appId = appId;
        this.childList = childList;
    }
}
