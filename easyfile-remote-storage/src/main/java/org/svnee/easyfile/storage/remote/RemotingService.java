package org.svnee.easyfile.storage.remote;

public interface RemotingService {

    /**
     * 服务开启
     */
    void start();

    /**
     * 服务关闭
     */
    void shutdown();

    /**
     * 注册RPC钩子
     *
     * @param rpcHook 钩子
     */
    void registerRPCHook(RPCHook rpcHook);

}
