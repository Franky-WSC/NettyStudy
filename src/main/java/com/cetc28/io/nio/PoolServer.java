package com.cetc28.io.nio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Auther: WSC
 * @Date: 2022/1/29 - 01 - 29 - 21:03
 * @Description: com.cetc28.io.nio
 * @version: 1.0
 */
public class PoolServer {
    //定义一个线程池, 该线程池内有50个线程(工人)等着在那干活
    ExecutorService pool = Executors.newFixedThreadPool(50);
    //有自己的selector
    private Selector selector;

    // 这是程序的main函数:入口函数
    public static void main(String[] args) throws IOException {
        PoolServer server = new PoolServer();
        server.initServer(8000);
        server.listen();
    }

    private void initServer(int port) throws IOException {
        //ServerSocketChannel: NIO中对BIO ServerSocket的封装, 还是那个通道, 但是双向, 可以同时读写(不用每次都先取出is/os)
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress("localhost",port));
        ssc.configureBlocking(false);//设定阻塞为false: 非阻塞模型
        System.out.println("服务端启动成功!");

        this.selector = Selector.open();//打开一个selector
        ssc.register(selector, SelectionKey.OP_ACCEPT);//注册轮询时对哪件事情感兴趣,如果发生这件事就进行处理
    }

    private void listen() throws IOException {
        while(true){
            //轮询发现有注册的事件发生,进行处理
            selector.select();//阻塞方法1:轮训一遍,当发生注册时的事件时(此时,就是上述的sccept),进行下一步处理;否则阻塞......
            Set<SelectionKey> keys = this.selector.selectedKeys();//轮询时符合条件的事件key放入这个set集合中
            Iterator<SelectionKey> ite = keys.iterator();
            while(ite.hasNext()){
                SelectionKey key = ite.next();
                ite.remove();//处理掉当前key就remove掉,否则下次轮询还会处理一次
                //首先判断key是个什么事情
                if(key.isAcceptable()){//是个accept事件(之前注册过的),说明此时有客户端想连接进来
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();//类似于BIO中的serversocket
                    SocketChannel sc = ssc.accept();//类似于BIO的accept,正式建立了连接
                    sc.configureBlocking(false);//设定阻塞为false: 非阻塞模型(否则这个通道依旧是那种阻塞模型)
                    sc.register(key.selector(), SelectionKey.OP_READ);//在这个通道上我再来监控read事件,下次轮询就可以处理read
                }else if(key.isReadable()){//是个read事件(在accept之后注册过的),说明此刻这个通道有要读取的消息
                    //如果可以读?
                    key.interestOps(key.interestOps()&(~SelectionKey.OP_READ));
                    //关键核心代码: 把任务交给线程池去处理(线程池里头自己选,谁空着谁就来处理)
                    pool.execute(new ThreadHandlerChannel(key));
                }
            }
        }
    }

    class ThreadHandlerChannel extends Thread {
        private SelectionKey key;

        public ThreadHandlerChannel(SelectionKey key) {
            this.key = key;
        }

        @Override
        public void run() {
            //类似于BIO的accept返回的那个socket对象,代表那个通道,但是双向,可以同时读写
            SocketChannel sc = (SocketChannel) key.channel();
            //NIO模型中,每一个通道都是和一个buffer连在一起,效率提高很多
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                int size = 0;
                while((size = sc.read(buffer)) > 0){//与BIO的socket不同,不用先取出inputstream就可以读取;
                    buffer.flip();
                    baos.write(buffer.array(),0,size);
                    buffer.clear();
                }
                baos.close();

                byte[] content = baos.toByteArray();
                //ByteBuffer: 很难用的一个类, NIO不受欢迎的原因之一, Netty对其进行了封装收到欢迎
                ByteBuffer bufferToWrite = ByteBuffer.allocate(content.length);
                bufferToWrite.put(content);
                bufferToWrite.flip();
                sc.write(bufferToWrite);//与BIO的socket不同,不用先取出outputstream就可以写入;
                if(size == -1){
                    sc.close();
                }else{
                    key.interestOps(key.interestOps()|SelectionKey.OP_READ);
                    key.selector().wakeup();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
