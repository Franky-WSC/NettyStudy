package com.cetc28.BIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: WSC
 * @Date: 2022/1/28 - 01 - 28 - 20:25
 * @Description: com.cetc28.BIO
 * @version: 1.0
 */
public class Server {
    // 这是BIO的server方式
    public static void main(String[] args) {
        try {
            ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(8888));

            Socket s = ss.accept();
            System.out.println("a client connect!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
