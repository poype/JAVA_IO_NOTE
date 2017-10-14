package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Server {
    public static void main(String[] args) throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        SocketAddress bindAddress = new InetSocketAddress("localhost",9999);
        serverSocketChannel.bind(bindAddress);
        ByteBuffer recvBuffer = ByteBuffer.allocate(100);
        ByteBuffer sendBuffer = ByteBuffer.allocate(100);
        sendBuffer.put("Hello,Welcome".getBytes());
        sendBuffer.flip();
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("接受一个连接");
            int bytesRead = socketChannel.read(recvBuffer);
            while(bytesRead != -1) {
                recvBuffer.flip(); //一定要调用
                while(recvBuffer.hasRemaining()) {
                    System.out.print((char)recvBuffer.get());
                }
                recvBuffer.clear(); //一定要调用
                //在网络中，下面这种调用是有问题的。因为如果客户端已经发送完了数据，那么服务端就会一直阻塞在这里
                //可以对比一下微信，微信一次发送的消息长度是有一个最大值的。
                //可以猜测后端有一块固定大小的buffer，客户端一次发送的数据量不能超过buffer的大小。
                //除非定义复杂的通信协议，否则服务端无法知道客户端是否还有数据发送
                bytesRead = socketChannel.read(recvBuffer);
                System.out.println(bytesRead);
            }
            System.out.println("send begin");
            socketChannel.write(sendBuffer);
            System.out.println("send end");
            socketChannel.close();
        }
    }
}
