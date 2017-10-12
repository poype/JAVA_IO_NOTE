package com.poype.socket;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",10086);
        OutputStream os = socket.getOutputStream();
        InputStream is = socket.getInputStream();
        writeData(os, "ping");
        String serverMessage = readData(is);
        System.out.println(serverMessage);
    }

    private static String readData(InputStream is) throws IOException {
        byte[] buf = new byte[100];
        int count = is.read(buf);
        byte[] clientValue = Arrays.copyOf(buf,count);
        return new String(clientValue);
    }

    private static void writeData(OutputStream os, String value) throws IOException {
        byte[] buf = value.getBytes();
        os.write(buf);
    }
}
