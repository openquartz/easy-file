package org.svnee.easyfile.storage.remote;

import org.svnee.easyfile.common.bean.Pair;
import org.svnee.easyfile.storage.remote.exception.RemotingSendRequestException;
import org.svnee.easyfile.storage.remote.exception.RemotingTimeoutException;
import org.svnee.easyfile.storage.remote.exception.RemotingTooMuchRequestException;
import org.svnee.easyfile.storage.remote.netty.NettyRequestProcessor;
import org.svnee.easyfile.storage.remote.protocol.RemotingCommand;
import io.netty.channel.Channel;
import java.util.concurrent.ExecutorService;

/**
 * 远程服务
 *
 * @author svnee
 */
public interface RemotingServer extends org.svnee.easyfile.storage.remote.RemotingService {

    /**
     * 注册请求Code 的请求解析逻辑
     *
     * @param requestCode 请求code
     * @param processor 请求处理
     * @param executor 执行器
     */
    void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
        final ExecutorService executor);

    /**
     * 注册默认的请求结果处理器
     * 兜底请求处理
     *
     * @param processor 请求处理器
     * @param executor 执行器
     */
    void registerDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);

    /**
     * 本地监听端口
     *
     * @return 端口
     */
    int localListenPort();

    /**
     * 获取请求code的处理执行器
     *
     * @param requestCode 请求code
     * @return 请求处理器 key: 请求处理逻辑,value: 线程执行器
     */
    Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(final int requestCode);

    /**
     * 同步发送消息,等待请求结果返回
     */
    RemotingCommand invokeSync(final Channel channel, final RemotingCommand request,
        final long timeoutMillis) throws InterruptedException, RemotingSendRequestException,
        RemotingTimeoutException;

    /**
     * 异步执行
     */
    void invokeAsync(final Channel channel, final RemotingCommand request, final long timeoutMillis,
        final org.svnee.easyfile.storage.remote.InvokeCallback invokeCallback) throws InterruptedException,
        RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    /**
     * 执行一次不关心成功失败,用于通知
     */
    void invokeOneway(final Channel channel, final RemotingCommand request, final long timeoutMillis)
        throws InterruptedException, RemotingTooMuchRequestException, RemotingTimeoutException,
        RemotingSendRequestException;

}
