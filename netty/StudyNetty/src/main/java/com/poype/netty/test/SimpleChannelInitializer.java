package com.poype.netty.test;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

// 简单的通道初始化处理器（暂时为空实现）
class SimpleChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) {
        // 添加字符串编解码器（基于UTF-8）
        ch.pipeline().addLast(new StringDecoder());
        ch.pipeline().addLast(new StringEncoder());
        // 添加自定义的业务Handler
        ch.pipeline().addLast(new NioServerHandler());
        ch.pipeline().addLast(new TestServerHandler1());
        ch.pipeline().addLast(new TestServerHandler2());
    }
}
