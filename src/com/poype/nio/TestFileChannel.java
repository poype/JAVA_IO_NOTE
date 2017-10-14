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

/*
 对Buffer的一些理解。
 Buffer有两种模式——读模式和写模式。
 读模式是指数据读入到Buffer中，典型的就是调用channel的read方法将数据读入到Buffer中。
 写模式是指从Buffer中取数据，例如调用channel的write方法，或者调用Buffer的get方法等等。
 刚构建好一个Buffer之后，它的默认模式就是读模式。这也很容易理解，毕竟一个空的Buffer没什么用处。
 调用Buffer的flip()方法将这个Buffer从读模式切换的写模式。
 调用Buffer的clear()或compact()方法，将一个Buffer从写模式切换到读模式。
 */