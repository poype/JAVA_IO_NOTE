package com.poype.netty.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class NioServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // 读取客户端消息并打印
        System.out.println("接收到客户端消息: " + msg);
        // 打印客户端远程地址
        System.out.println("客户端地址: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // 消息接收完成后，向客户端返回响应
        String response = "Hello client";
        ctx.writeAndFlush(response); // 自动使用StringEncoder编码为UTF-8
        System.out.println("已向客户端发送响应: " + response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常处理：打印异常信息并关闭通道
        System.err.println("客户端连接异常: " + cause.getMessage());
        ctx.close();
    }
}
