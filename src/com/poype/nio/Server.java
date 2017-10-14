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
        ByteBuffer buffer = ByteBuffer.allocate(10);
        while (true) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            System.out.println("接受一个连接");
            int bytesRead = socketChannel.read(buffer);
            while(bytesRead != -1) {
                buffer.flip(); //一定要调用
                while(buffer.hasRemaining()) {
                    System.out.print((char)buffer.get());
                }
                buffer.clear(); //一定要调用
                bytesRead = socketChannel.read(buffer);
            }
            socketChannel.close();
        }
    }
}
