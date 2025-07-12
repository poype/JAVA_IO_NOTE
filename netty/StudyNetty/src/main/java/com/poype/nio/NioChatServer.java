package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NioChatServer {
    // 服务器端口和字符集
    private static final int PORT = 8888;
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    // 选择器和客户端通道集合
    private Selector selector;
    private final Map<SocketChannel, String> clientNames = new HashMap<>(); // 存储客户端通道和用户名

    public void start() {
        try {
            // 1. 打开选择器
            selector = Selector.open();

            // 2. 打开服务器通道并配置
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));

            // 3. 将服务器通道注册到选择器，监听连接事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("NIO群聊服务器已启动，监听端口: " + PORT);

            // 4. 处理客户端连接和消息
            while (true) {
                // 等待事件发生，设置超时时间避免阻塞
                int readyChannels = selector.select(1000);
                if (readyChannels == 0) {
                    continue;
                }

                // 获取所有就绪的选择键
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

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

                    // 移除已处理的键
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    // 处理客户端连接请求
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            // 设置客户端通道为非阻塞模式
            clientChannel.configureBlocking(false);
            // 注册客户端通道到选择器，监听读事件
            SelectionKey clientKey = clientChannel.register(selector, SelectionKey.OP_READ);

            // 欢迎新客户端
            String welcomeMsg = "欢迎加入群聊！请先输入你的用户名:";
            sendToClient(clientChannel, welcomeMsg);

            System.out.println("新客户端连接: " + clientChannel.getRemoteAddress());
        }
    }

    // 处理客户端消息读取
    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // 读取客户端消息
        int read = clientChannel.read(buffer);
        if (read > 0) {
            buffer.flip();
            String message = CHARSET.decode(buffer).toString().trim();

            // 处理用户名设置
            if (!clientNames.containsKey(clientChannel)) {
                handleUsername(clientChannel, message);
                return;
            }

            // 处理退出命令
            if (message.startsWith("/exit")) {
                handleExit(clientChannel);
                return;
            }

            // 广播消息给其他客户端
            broadcastMessage(clientChannel, clientNames.get(clientChannel) + ": " + message);
        } else if (read == -1) {
            // 客户端断开连接
            handleExit(clientChannel);
        }
    }

    // 处理客户端用户名设置
    private void handleUsername(SocketChannel clientChannel, String username) {
        if (username.isEmpty()) {
            sendToClient(clientChannel, "用户名不能为空，请重新输入:");
            return;
        }

        // 检查用户名是否已存在
        if (clientNames.values().contains(username)) {
            sendToClient(clientChannel, "用户名已存在，请更换用户名:");
            return;
        }

        // 保存用户名
        clientNames.put(clientChannel, username);
        String welcomeMsg = "欢迎 " + username + " 加入群聊！当前在线人数: " + clientNames.size();

        // 广播新用户加入消息
        broadcastMessage(null, welcomeMsg);
        sendToClient(clientChannel, "你已成功加入群聊！");

        System.out.println("用户 " + username + " 已连接");
    }

    // 处理客户端退出
    private void handleExit(SocketChannel clientChannel) {
        if (clientNames.containsKey(clientChannel)) {
            String username = clientNames.get(clientChannel);
            clientNames.remove(clientChannel);

            // 广播用户退出消息
            broadcastMessage(null, "用户 " + username + " 已退出群聊，当前在线人数: " + clientNames.size());
            System.out.println("用户 " + username + " 已退出");
        }

        // 关闭通道
        try {
            if (clientChannel != null && clientChannel.isOpen()) {
                clientChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 向指定客户端发送消息
    private void sendToClient(SocketChannel clientChannel, String message) {
        try {
            ByteBuffer buffer = CHARSET.encode(message + "\n");
            clientChannel.write(buffer);
        } catch (IOException e) {
            System.err.println("向客户端发送消息失败: " + e.getMessage());
            handleExit(clientChannel);
        }
    }

    // 广播消息给所有客户端（除了发送者）
    private void broadcastMessage(SocketChannel sender, String message) {
        System.out.println("广播消息: " + message);

        for (SocketChannel client : clientNames.keySet()) {
            // 不发送给消息发送者
            if (client != sender) {
                sendToClient(client, message);
            }
        }
    }

    // 关闭服务器资源
    private void close() {
        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
            }
            System.out.println("服务器已关闭");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        NioChatServer server = new NioChatServer();
        server.start();
    }
}
