package com.cetc28.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @Auther: WSC
 * @Date: 2022/1/30 - 01 - 30 - 11:06
 * @Description: com.cetc28.netty
 * @version: 1.0
 */
public class Client {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        new Client().clientStart();
    }

    private void clientStart() {
        //Client也可以多线程, 参数中指定即可, 默认一个
        EventLoopGroup workers = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(workers)
                .channel(NioSocketChannel.class)//指定通道类型是NioSocketChannel
                .handler(new ChannelInitializer<SocketChannel>() {
                    //通道初始化时我们该干什么
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        System.out.println("通道初始化完毕");
                        //加入一个新的监听器, 建立连接之后的IO操作
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });

        try {
            System.out.println("开始连接服务器");
            ChannelFuture f = b.connect("localhost",8888).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workers.shutdownGracefully();
        }
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter{
    //一旦通道建立之后的操作
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("通道已被激活");

        //一旦通道建立之后,就往外写一个数据
        final ChannelFuture f = ctx.writeAndFlush(Unpooled.copiedBuffer("你好服务器".getBytes()));
        //又加了一个监听器
        f.addListener(new ChannelFutureListener() {
            @Override//当写操作完成后执行的函数
            public void operationComplete(ChannelFuture future) throws Exception {
                System.out.println("消息已送达, 等待服务器返回消息");
            }
        });
    }

    //一旦通道有读取完数据后的操作
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            System.out.println(buf.toString());
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
}