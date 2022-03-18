package org.svnee.easyfile.storage.remote;

import io.netty.channel.Channel;

/**
 * Netty 通道消息事件监听器
 */
public interface ChannelEventListener {

    /**
     * 通道连接时
     */
    void onChannelConnect(final String remoteAddr, final Channel channel);

    /**
     * 通道关闭时
     */
    void onChannelClose(final String remoteAddr, final Channel channel);

    /**
     * 通道异常时
     */
    void onChannelException(final String remoteAddr, final Channel channel);

    /**
     * 通道闲置时
     *
     * @param remoteAddr 远程地址
     * @param channel 通道
     */
    void onChannelIdle(final String remoteAddr, final Channel channel);
}
