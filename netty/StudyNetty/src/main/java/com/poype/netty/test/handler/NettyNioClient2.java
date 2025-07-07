package com.poype.netty.test.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.util.Scanner;

public class NettyNioClient2 {
    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            // 添加客户端处理器
                            pipeline.addLast(new ClientInboundHandler());
                            pipeline.addLast(new ClientOutboundHandler());
                        }
                    });

            // 连接服务器
            ChannelFuture f = b.connect("localhost", 8888).sync();
            System.out.println("客户端已连接到服务器");

            // 获取通道
            Channel channel = f.channel();
            // 启动控制台输入线程，发送消息到服务器
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入消息（输入'exit'退出）:");
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                if ("exit".equals(msg)) {
                    break;
                }
                // 发送消息到服务器
                channel.writeAndFlush(msg);
            }

            // 关闭连接
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
            System.out.println("客户端已关闭");
        }
    }

    // 客户端入站处理器：处理服务器响应
    static class ClientInboundHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            System.out.println("客户端收到服务器响应: " + msg);
        }
    }

    // 客户端出站处理器：处理客户端发送的消息
    static class ClientOutboundHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("客户端发送消息: " + msg);
            Thread.sleep(10000);
            super.write(ctx, msg, promise);
        }
    }
}