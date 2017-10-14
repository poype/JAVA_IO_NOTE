package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

public class Client {
    public static void main(String args[]) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        ByteBuffer buffer = ByteBuffer.allocate(200);
        buffer.put("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".getBytes());
        socketChannel.connect(new InetSocketAddress("localhost",9999));
        buffer.flip();
        socketChannel.write(buffer);
        socketChannel.close();
    }
}
