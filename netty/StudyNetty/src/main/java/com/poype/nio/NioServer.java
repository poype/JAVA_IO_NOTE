package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class NioServer {
    // 缓冲区大小
    private static final int BUFFER_SIZE = 1024;
    // 监听端口
    private static final int PORT = 8888;

    public void start() {
        // 1. 创建 ServerSocketChannel
        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // 设置为非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
            System.out.println("NIO 服务端已启动，监听端口：" + PORT);

            // 2. 创建 Selector
            Selector selector = Selector.open();
            // 3. 将 ServerSocketChannel 注册到 Selector 上，监听连接事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            // 4. 轮询选择键
            while (true) {
                // 等待事件发生，阻塞直到有事件就绪
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                // 获取所有就绪的选择键
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    // 处理连接事件
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    // 处理读事件
                    else if (key.isReadable()) {
                        handleRead(key);
                    }

                    // 从已选择的键集合中移除当前键
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 处理客户端连接
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // 接受客户端连接，得到 SocketChannel
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel != null) {
            System.out.println("客户端连接成功：" + socketChannel.getRemoteAddress());
            // 设置为非阻塞模式
            socketChannel.configureBlocking(false);
            // 将 SocketChannel 注册到 Selector 上，监听读事件
            socketChannel.register(key.selector(), SelectionKey.OP_READ, ByteBuffer.allocate(BUFFER_SIZE));
        }
    }

    // 处理客户端读事件
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear(); // 清空缓冲区

        // 从客户端读取数据
        int bytesRead = socketChannel.read(buffer);

        if (bytesRead > 0) {
            buffer.flip(); // 切换到读模式
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8);
            System.out.println("收到客户端消息：" + message);
        } else if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("客户端断开连接：" + socketChannel.getRemoteAddress());
            socketChannel.close();
        }
    }

    public static void main(String[] args) {
        NioServer server = new NioServer();
        server.start();
    }
}