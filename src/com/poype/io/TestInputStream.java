package com.poype.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/*
 InputStream是一个抽象类，它是所有输入流的老祖宗
 它只有一个方法是抽象的，public abstract int read() throws IOException;它的含义是返回输入流中的下一个字节。如果没有字节可以读取了则返回-1。
 InputStream类中其它的非抽象方法都是依赖这个抽象方法read()实现的
 例如int read(byte b[], int off, int len)，这个方法就是每次调用read()读取一个字节，然后将读到的字节添加的数组中的正确位置。
 再如long skip(long n)，这个方法也是调用read方法读取流中的前n个字节，只是对读到的字节没有做任何处理，以此达到跳过流中前n个字节的效果。
 可见，对于输入流来说，只能顺序依次读取流中的字节，不能随机读取。这一点与NIO不同
 此外，对于流来说，操纵的都是字节。
 */
public class TestInputStream {

    public static void main(String[] args) throws IOException {
        String fileName = "/Users/poype/ideaWorkspace/StudyIO/src/com/poype/io/TestInputStream.java";

        InputStream is = new FileInputStream(fileName);
        int ch = is.read();
        while(ch != -1) {
            System.out.print((char)ch);//会导致中文乱码
            ch = is.read();
        }
    }
}
