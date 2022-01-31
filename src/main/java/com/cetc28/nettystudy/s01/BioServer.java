package com.cetc28.nettystudy.s01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SocketChannel;

/**
 * @Auther: WSC
 * @Date: 2022/1/30 - 01 - 30 - 17:22
 * @Description: com.cetc28.nettystudy.s01
 * @version: 1.0
 */
public class BioServer {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) throws IOException {
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress(8888));

        Socket s = ss.accept();
        System.out.println("一个客户端连接成功");
    }
}
