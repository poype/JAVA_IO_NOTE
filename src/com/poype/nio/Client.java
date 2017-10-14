package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {
    public static void main(String args[]) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false); //修改socketChannel为异步模式
        ByteBuffer buffer = ByteBuffer.allocate(200);
        buffer.put("1234567890abedefg".getBytes());
        socketChannel.connect(new InetSocketAddress("localhost",9999));
        while(!socketChannel.finishConnect()) {
            System.out.println("还没有建立好connect"); //这句话没有打印，可能是connect的动作在本机上太快了
        }
        buffer.flip();
        socketChannel.write(buffer);
        buffer.clear();
        // read()方法返回-1表示已经读到尾，返回0表示没有读取到数据
        while(socketChannel.read(buffer) <= 0) {
            System.out.println("还没读取到数据");
        }
        buffer.flip();
        while(buffer.hasRemaining()) {
            System.out.print((char)buffer.get());
        }
        socketChannel.close();
    }
}
