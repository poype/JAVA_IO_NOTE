package com.poype.io;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestBufferedInputStream {
    public static void main(String[] args) throws IOException {
        String fileName = "/Users/poype/ideaWorkspace/StudyIO/src/com/poype/io/test";
        FileInputStream fis = new FileInputStream(fileName);
        BufferedInputStream bis = new BufferedInputStream(fis);
        long start = System.currentTimeMillis();
        int ch = bis.read();
        while(ch != -1) {
            ch = bis.read();
        }
        long end = System.currentTimeMillis();
        System.out.println(end - start);
        bis.close(); //调用BufferedInputStream对象的close方法，被装饰对象的close方法也会被调用。所以此处无需调用fis.close。
    }
}

/*
 使用BufferedInputStream装饰FileInputStream，使读取数据更快
 test.html文件是用来作为测试的数据文件。
 经过测试，使用BufferedInputStream的读取速度比单独使用FileInputStream要快10倍以上。
 注意，程序中已经调用了bis.close()，无需再调用fis.close()。
 */

