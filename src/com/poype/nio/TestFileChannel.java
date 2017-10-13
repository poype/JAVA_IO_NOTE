package com.poype.nio;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*
 借助nio实现的一个文件copy程序
 FileChannel不能设置为非阻塞模式
 */
public class TestFileChannel {
    public static void main(String[] args) throws IOException {
        RandomAccessFile inFile = new RandomAccessFile("/Users/poype/ideaWorkspace/StudyIO/pic/输入流.png","rw");
        RandomAccessFile outFile = new RandomAccessFile("/Users/poype/Desktop/ldl.png","rw");
        FileChannel readChannel = inFile.getChannel();
        FileChannel writeChannel = outFile.getChannel();
        ByteBuffer buffer = ByteBuffer.allocate(200);
        int bytesRead = readChannel.read(buffer);
        while(bytesRead != -1) {
            buffer.flip(); // 注意翻转buffer的状态，否则数据写入的不对
            writeChannel.write(buffer);
            buffer.clear(); // 清空buffer，否则无法继续读入新的数据
            bytesRead = readChannel.read(buffer);
        }
        readChannel.close();
        writeChannel.close();
    }
}
