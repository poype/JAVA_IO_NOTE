package com.poype.socket;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket();
        SocketAddress address = new InetSocketAddress("localhost",10086);
        serverSocket.bind(address);
        while(true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("接受到一个client连接");
            InputStream is = clientSocket.getInputStream();
            OutputStream os = clientSocket.getOutputStream();
            String clientMessage = readData(is);
            System.out.println("receive client message:" + clientMessage);
            TimeUnit.MILLISECONDS.sleep(3000);
            if("ping".equals(clientMessage)) {
                writeData(os, "pong");
            } else {
                writeData(os, "I don't know what client said");
            }
            is.close();
            os.close();
        }
    }

    // 从输入流中读取数据
    private static String readData(InputStream is) throws IOException {
        byte[] buf = new byte[100];
        int count = is.read(buf);
        byte[] clientValue = Arrays.copyOf(buf,count);
        return new String(clientValue);
    }

    // 向输出流写出数据
    private static void writeData(OutputStream os, String value) throws IOException {
        byte[] buf = value.getBytes();
        os.write(buf);
    }
}
