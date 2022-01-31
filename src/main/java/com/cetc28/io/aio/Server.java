package com.cetc28.io.aio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @Auther: WSC
 * @Date: 2022/1/30 - 01 - 30 - 9:44
 * @Description: com.cetc28.io.aio
 * @version: 1.0
 */
public class Server {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) throws IOException, InterruptedException {
        //AIO: AsynchronousServerSocketChannel 不管怎么叫,还是那块面板
        final AsynchronousServerSocketChannel assc = AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(8888));
        //accept: 方法不再阻塞,调用之后马上向下运行
        //CompletionHandler: 观察者模式/回调函数: 当发生要连接的事情时,你给我去调用我交给你的方法;
        assc.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            //completed: 如果客户端连接上了,接下来的处理
            @Override
            public void completed(AsynchronousSocketChannel client, Object attachment) {
                assc.accept(null,this);//如果不写这段代码,下一个客户端连接不上来
                try {
                    System.out.println(client.getRemoteAddress());//打印连接上来的客户端的ip地址
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    //read: 方法不再阻塞,调用之后马上向下运行
                    //CompletionHandler: 观察者模式/回调函数: 当发生读取的事情时,你给我去调用我交给你的方法;
                    client.read(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                        //completed: 如果读完了,接下来怎么处理
                        @Override
                        public void completed(Integer result, ByteBuffer attachment) {
                            attachment.flip();
                            //把收到的信息进行打印
                            System.out.println(new String(attachment.array(),0,result));
                            //同时把消息给客户端传回去
                            client.write(ByteBuffer.wrap("你好客户端".getBytes()));
                        }
                        //failed: 如果读取失败了,接下来怎么处理
                        @Override
                        public void failed(Throwable exc, ByteBuffer attachment) {
                            exc.printStackTrace();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //failed:如果客户端没有连接上,接下来的处理
            @Override
            public void failed(Throwable exc, Object attachment) {
                exc.printStackTrace();//失败了,把异常交给你,自己去处理
            }
        });

        //为了防止程序调用完accept之后就结束(accept不再阻塞)
        while (true){
            Thread.sleep(1000);
        }
    }
}
