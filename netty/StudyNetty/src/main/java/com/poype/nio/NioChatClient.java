package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class NioChatClient {
    // 服务器地址和端口
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8888;
    // 字符集
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    // 客户端选择器和通道
    private SocketChannel socketChannel;
    private String username;

    public void start(String username) {
        this.username = username;
        try {
            // 1. 打开 socket 通道
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false); // 设置为非阻塞模式

            // 2. 连接服务器
            if (!socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT))) {
                // 非阻塞连接，需要等待连接完成
                while (!socketChannel.finishConnect()) {
                    System.out.println("正在连接服务器...");
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            }
            System.out.println("成功连接到服务器！");

            // 3. 启动接收消息线程
            new Thread(this::receiveMessages).start();

            // 4. 启动发送消息线程
            new Thread(this::sendMessages).start();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            close();
        }
    }

    // 接收服务器消息的方法
    private void receiveMessages() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            while (true) {
                // 清空缓冲区
                buffer.clear();

                // 从通道读取数据到缓冲区
                int read = socketChannel.read(buffer);
                if (read > 0) {
                    // 切换缓冲区为读取模式
                    buffer.flip();
                    String message = CHARSET.decode(buffer).toString();
                    System.out.println(message);
                }
                // 短暂休眠避免CPU占用过高
                Thread.sleep(100);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("接收消息时出错: " + e.getMessage());
        } finally {
            close();
        }
    }

    // 发送消息到服务器的方法
    private void sendMessages() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (scanner.hasNextLine()) {
                String message = scanner.nextLine();
                if ("exit".equals(message.toLowerCase())) {
                    // 退出命令
                    sendToServer("/exit");
                    break;
                }
                // 发送带用户名的消息
                sendToServer(username + ": " + message);
            }
        } finally {
            scanner.close();
            close();
        }
    }

    // 向服务器发送消息的辅助方法
    private void sendToServer(String message) {
        try {
            ByteBuffer buffer = CHARSET.encode(message);
            socketChannel.write(buffer);
        } catch (IOException e) {
            System.err.println("发送消息时出错: " + e.getMessage());
            close();
        }
    }

    // 关闭资源
    private void close() {
        try {
            if (socketChannel != null) {
                socketChannel.close();
            }
            System.out.println("已断开与服务器的连接");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.print("请输入你的用户名: ");
        Scanner scanner = new Scanner(System.in);
        String username = scanner.nextLine();
        scanner.close();

        NioChatClient client = new NioChatClient();
        client.start(username);

        TimeUnit.SECONDS.sleep(511);
    }
}
