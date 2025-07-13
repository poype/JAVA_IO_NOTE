package com.poype.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 读取数据还是在主线程，但读取完数据后，会将数据交给一个线程池处理。
 * 单线程处理所有 IO 事件的监听和分发，避免多线程竞争问题
 * 将业务逻辑（如数据处理）提交到线程池，避免 Reactor 线程阻塞
 */
public class ReactorMultiThreadServer {
    // 端口号
    private static final int PORT = 8888;
    // 工作线程池大小（可根据CPU核心数调整）
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    // 工作线程池
    private final ExecutorService workerPool;
    // 选择器
    private final Selector selector;
    // 服务器通道
    private final ServerSocketChannel serverSocketChannel;

    public ReactorMultiThreadServer() throws IOException {
        // 初始化工作线程池
        workerPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        // 初始化选择器
        selector = Selector.open();
        // 初始化服务器通道
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false); // 设置为非阻塞模式
        serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
        // 注册连接事件到选择器
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Reactor多线程服务器启动，监听端口：" + PORT);
    }

    // 启动服务器
    public void start() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 等待事件发生，阻塞直到有事件就绪
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }

                // 获取所有就绪的事件
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    // 处理连接事件
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    // 处理可读事件
                    else if (key.isReadable()) {
                        handleRead(key);
                    }
                    // 处理可写事件（此处简化，实际可按需处理）
                    else if (key.isWritable()) {
                        handleWrite(key);
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            System.err.println("服务器运行异常：" + e.getMessage());
        } finally {
            stop();
        }
    }

    // 处理客户端连接事件
    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        if (clientChannel != null) {
            System.out.println("新客户端连接：" + clientChannel.getRemoteAddress());
            // 设置客户端通道为非阻塞模式
            clientChannel.configureBlocking(false);
            // 注册读事件到选择器，并附加客户端通道的上下文
            clientChannel.register(selector, SelectionKey.OP_READ,
                    ByteBuffer.allocate(1024));
        }
    }

    // 处理客户端可读事件
    private void handleRead(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        buffer.clear(); // 清空缓冲区以便读取新数据

        try {
            // 读取客户端数据
            int readBytes = clientChannel.read(buffer);
            if (readBytes > 0) {
                buffer.flip(); // 切换为读取模式
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String request = new String(data, "UTF-8");
                System.out.println("收到客户端请求：" + request);

                // 将数据处理任务提交到工作线程池
                workerPool.submit(() -> {
                    try {
                        // 模拟业务处理（实际应替换为具体逻辑）
                        String response = "服务器已接收：" + request + " (处理线程：" +
                                Thread.currentThread().getName() + ")";
                        // 处理完成后，注册写事件回客户端
                        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
                        clientChannel.register(selector, SelectionKey.OP_WRITE, responseBuffer);
                    } catch (IOException e) {
                        System.err.println("处理客户端请求异常：" + e.getMessage());
                        closeChannel(clientChannel);
                    }
                });
            } else if (readBytes == -1) {
                // 客户端断开连接
                System.out.println("客户端断开连接：" + clientChannel.getRemoteAddress());
                key.cancel();
                closeChannel(clientChannel);
            }
        } catch (IOException e) {
            System.err.println("读取客户端数据异常：" + e.getMessage());
            key.cancel();
            closeChannel(clientChannel);
        }
    }

    // 处理客户端可写事件（响应数据）
    private void handleWrite(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        try {
            if (buffer.hasRemaining()) {
                clientChannel.write(buffer); // 写入响应数据
            } else {
                // 数据写入完成，重新注册读事件
                clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
            }
        } catch (IOException e) {
            System.err.println("写入客户端响应异常：" + e.getMessage());
            key.cancel();
            closeChannel(clientChannel);
        }
    }

    // 关闭客户端通道
    private void closeChannel(Channel channel) {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (IOException e) {
            System.err.println("关闭通道异常：" + e.getMessage());
        }
    }

    // 停止服务器
    private void stop() {
        try {
            if (selector != null) {
                selector.close();
            }
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (workerPool != null && !workerPool.isShutdown()) {
                workerPool.shutdown();
            }
            System.out.println("服务器已停止");
        } catch (IOException e) {
            System.err.println("停止服务器异常：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            ReactorMultiThreadServer server = new ReactorMultiThreadServer();
            server.start();
        } catch (IOException e) {
            System.err.println("服务器启动失败：" + e.getMessage());
        }
    }
}
