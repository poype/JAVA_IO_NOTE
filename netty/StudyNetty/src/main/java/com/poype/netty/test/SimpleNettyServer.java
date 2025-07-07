package com.poype.netty.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SimpleNettyServer {
    public static void main(String[] args) {
        // 创建 boss 线程池（接收连接）和 worker 线程池（处理读写）
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);  // 通常 boss 线程池大小为 1
        EventLoopGroup workerGroup = new NioEventLoopGroup();  // worker 线程池默认使用 CPU 核心数 * 2

        try {
            // 创建服务端启动配置
            ServerBootstrap serverBootstrap = new ServerBootstrap();

            // 配置两个线程池
            serverBootstrap.group(bossGroup, workerGroup)
                    // 指定服务端通道类型为 NIO ServerSocketChannel
                    .channel(NioServerSocketChannel.class)
                    // handler是针对bossGroup的， 配置日志处理器，方便查看运行状态
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 设置连接队列长度为 128
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    // 设置 child handler（这里先使用一个简单的空处理器）
                    .childHandler(new SimpleChannelInitializer());

            // 绑定端口 6688 并启动服务端
            System.out.println("NIO 服务端正在启动，绑定端口 6688...");
            ChannelFuture channelFuture = serverBootstrap.bind(6688).sync();

            // 监听通道关闭
            System.out.println("NIO 服务端已启动，监听端口 6688");
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅关闭线程池
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("NIO 服务端已关闭");
        }
    }
}

