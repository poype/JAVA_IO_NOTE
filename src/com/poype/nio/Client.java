package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String args[]) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        ByteBuffer buffer = ByteBuffer.allocate(200);
        buffer.put("1234567890abedefg".getBytes());
        socketChannel.connect(new InetSocketAddress("localhost",9999));
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();
        while(buffer.hasRemaining()) {
            System.out.print((char)buffer.get());
        }
        socketChannel.close();
    }
}
