package com.cetc28.io.bio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Auther: WSC
 * @Date: 2022/1/29 - 01 - 29 - 20:11
 * @Description: com.cetc28.io.bio
 * @version: 1.0
 */
public class Server {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) throws IOException {
        //服务器端,建立了server的socket插座,等着客户端来插
        ServerSocket ss = new ServerSocket();
        ss.bind(new InetSocketAddress("localhost",8888));
        //无限循环
        while(true){
            //阻塞方法1:没有客户端连接,阻塞...直到某一个客户端连上了把这个线程唤醒为止;
            Socket s = ss.accept();//连上了之后,返回一个socket,这个socket就代表和那个客户端联系的通道
            //lamda表达式
            new Thread(()->{
                handle(s);
            }).start();
        }
    }

    static void handle(Socket s){
        try {
            byte[] bytes = new byte[1024];
            int len = s.getInputStream().read(bytes);//阻塞方法2:客户端只连不写,阻塞...
            System.out.println(new String(bytes,0,len));

            s.getOutputStream().write(bytes,0,len);//阻塞方法3:客户端不接收,阻塞...
            s.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
