package com.cetc28.io.bio;

import java.io.IOException;
import java.net.Socket;

/**
 * @Auther: WSC
 * @Date: 2022/1/29 - 01 - 29 - 20:11
 * @Description: com.cetc28.io.bio
 * @version: 1.0
 */
public class Client {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) throws IOException {
        //客户端socket初始化,服务器端的ip和port
        Socket s = new Socket("localhost",8888);
        s.getOutputStream().write("你好服务器".getBytes());//阻塞方法1:服务端不接收,阻塞...
        s.getOutputStream().flush();
        System.out.println("服务器接收到消息,接下来等待服务器返回的消息");
        byte[] bytes = new byte[1024];
        int len = s.getInputStream().read(bytes);//阻塞方法2:服务端不写,阻塞...
        System.out.println(new String(bytes,0,len));
        s.close();
    }
}
