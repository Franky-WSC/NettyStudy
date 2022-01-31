package com.cetc28.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;

/**
 * @Auther: WSC
 * @Date: 2022/1/30 - 01 - 30 - 11:06
 * @Description: com.cetc28.netty
 * @version: 1.0
 */
public class HelloNetty {
    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        new NettyServer(8888).serverStart();
    }
}

class NettyServer {
    int port = 8888;

    public NettyServer(int port) {
        this.port = port;
    }

    //Netty对于server的启动过程封装的十分优雅
    public void serverStart(){
        //两个线程池
        EventLoopGroup bossGroup = new NioEventLoopGroup();//bossGroup 线程池1: 相当于NIO中的大管家selector,专门处理客户端连接的
        EventLoopGroup workerGroup = new NioEventLoopGroup();//workerGroup 线程池2: 相当于NIO中的工人群体,专门处理连接之后的IO操作
        //server启动的封装类: 要启动server, 通过ServerBootstrap来进行启动时的一些配置
        ServerBootstrap b = new ServerBootstrap();

        b.group(bossGroup,workerGroup)//指定这两个group: 第一个负责连接, 第二个负责连接之后的IO处理
                .channel(NioServerSocketChannel.class)//指定客户端建立连接的那个通道是什么类型
                .childHandler(new ChannelInitializer<SocketChannel>() {//当每个客户端连接上之后的监听器处理
                    //建立连接之后,通道初始化的操作
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //在这个通道上再加上一个新的监听器,用于连接之后的处理
                        //发现连接的代码和业务代码可以分开,互相之间不影响
                        ch.pipeline().addLast(new Handler());
                    }
                });

        try {
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

class Handler extends ChannelInboundHandlerAdapter{
    //处理过程: 重写channelRead等这些方法
    @Override//channelRead:数据都读好传进来了, 读数据过程都不用处理, 只需要写处理数据的过程就好
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("服务器: 通道已读取完毕");
        ByteBuf buf = (ByteBuf)msg;
        //当前处理过程: 先把接受到的数据打印出来, 然后给客户端写回去
        System.out.println(buf.toString(CharsetUtil.UTF_8));
        ctx.writeAndFlush(msg);
        ctx.close();
    }

    @Override//netty所有的异常处理都在这个方法里, 只要有异常, 就会回调这个函数, 所以我们就在这里处理
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();//一般的处理: 哪个通道出现了异常,哪个通道就要关闭
    }
}