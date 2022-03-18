package org.svnee.easyfile.storage.remote;


import org.svnee.easyfile.storage.remote.exception.RemotingCommandException;

public interface CommandCustomHeader {

    /**
     * 校验字段
     */
    void checkFields() throws RemotingCommandException;

}
