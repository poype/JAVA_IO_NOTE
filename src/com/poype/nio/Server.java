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
        ByteBuffer recvBuffer = ByteBuffer.allocate(100);  //接收buffer
        ByteBuffer sendBuffer = ByteBuffer.allocate(100);  //发送buffer
        sendBuffer.put("Hello,Welcome".getBytes());        //要发送的信息
        sendBuffer.flip();
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            int bytesRead = socketChannel.read(recvBuffer);
            if(bytesRead != -1) {
                recvBuffer.flip(); //一定要调用
                while(recvBuffer.hasRemaining()) {
                    System.out.print((char)recvBuffer.get());
                }
                System.out.println();
                recvBuffer.clear(); //一定要调用
            }
            int bytesWrite = socketChannel.write(sendBuffer);
            sendBuffer.rewind(); //此处必须调用rewind，发送的时候是从buffer的position位置开始。
            socketChannel.close();
        }
    }
}
