package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NioClient {
    private static final String SERVER_HOST = "127.0.0.1"; // 服务端IP
    private static final int SERVER_PORT = 8888; // 服务端端口

    public static void main(String[] args) {
        try (SocketChannel socketChannel = SocketChannel.open()) {
            // 1. 连接服务端
            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));
            System.out.println("客户端已连接到服务端");

            // 2. 准备要发送的消息
            String message = "Hello World";
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));

            // 3. 发送消息到服务端
            socketChannel.write(writeBuffer);
            System.out.println("已发送消息: " + message);

            // 4. 接收服务端响应（可选）
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int bytesRead = socketChannel.read(readBuffer);
            if (bytesRead > 0) {
                readBuffer.flip();
                String response = StandardCharsets.UTF_8.decode(readBuffer).toString();
                System.out.println("收到服务端响应: " + response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
