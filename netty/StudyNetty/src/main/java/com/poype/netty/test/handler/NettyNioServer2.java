package com.poype.netty.test.handler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyNioServer2 {
    public static void main(String[] args) {
        // bossGroup处理客户端连接请求，workerGroup处理具体IO操作
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            // 添加处理器到pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            // 字符串编解码器（处理字节与字符串转换）
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            // 入站处理器（处理客户端消息）
                            pipeline.addLast(new InboundHandler1());
                            pipeline.addLast(new InboundHandler2());
                            // 出站处理器（处理服务器响应）
                            pipeline.addLast(new OutboundHandler());
                        }
                    });

            // 绑定端口并启动服务
            ChannelFuture f = b.bind(8888).sync();
            System.out.println("Netty NIO 服务端启动，监听端口: 8888");

            // 等待服务端关闭
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
            System.out.println("服务端已关闭");
        }
    }

    // 入站处理器1：处理客户端消息
    static class InboundHandler1 extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("InboundHandler1 收到消息: " + msg);
            // 传递消息到下一个处理器
            ctx.fireChannelRead(msg);
        }
    }

    // 入站处理器2：处理客户端消息
    static class InboundHandler2 extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("InboundHandler2 收到消息: " + msg);
            // 传递消息到下一个处理器
            ctx.fireChannelRead(msg);

            ctx.channel().writeAndFlush("server server server server server server");
        }
    }

    // 出站处理器：处理服务器响应
    static class OutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("OutboundHandler 发送响应: " + msg);
            Thread.sleep(10000);
            // 调用父类方法继续处理出站操作
            super.write(ctx, msg, promise);
        }
    }
}