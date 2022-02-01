package com.cetc28.nettystudy.s02;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.lang.ref.Reference;
import java.net.InetSocketAddress;

/**
 * @Auther: WSC
 * @Date: 2022/1/31 - 01 - 31 - 12:31
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class Server {
    //用一个默认的线程来处理通道组上那些事件: 这个通道组保存所有已经连接上的客户端通道
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 这是程序的main函数:入口函数
    public static void main(String[] args) {

    }

    public void serverStart() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        try {
            ChannelFuture cf = b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
//                            System.out.println("ChannelInitializer时的SocketChannel: " + ch);
                            ServerFrame.INSTANCE.updateServerMsg("ChannelInitializer时的SocketChannel: " + ch);
                            //给当前连接进的channel添加责任处理器: 一般业务逻辑处理加到最后 编码解码等处理加到前面
                            ChannelPipeline cp = ch.pipeline();
                            cp//.addLast(new TankMsgDecoder())
                                    .addLast(new MyServerHandler());
                        }
                    })
                    .bind(new InetSocketAddress(8888))
                    .sync();
//            System.out.println("服务器启动成功!");
            ServerFrame.INSTANCE.updateServerMsg("服务器启动成功");
            //让server阻塞,防止程序退出
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

class MyServerHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("channelActive时的ctx.channel(): " + ctx.channel());
        ServerFrame.INSTANCE.updateServerMsg("channelActive时的ctx.channel(): " + ctx.channel());
        //将当前连接的channel添加到ChannelGroup中
        Server.clients.add(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println("channelRead");
//        try {
//            TankMsg tm = (TankMsg)msg;
//            ServerFrame.INSTANCE.updateClientMsg(tm.toString());//收到的消息显示在主面板右边
//        }finally {
//            ReferenceCountUtil.release(msg);
//        }

        ByteBuf buf = (ByteBuf) msg;
        //将客户端发来的消息在服务器中打印显示
        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(),bytes);
        String str = new String(bytes);//收到的消息转化成的字符串
        ServerFrame.INSTANCE.updateClientMsg(str);//收到的消息显示在主面板右边
        if ("_bye_".equals(str)){
            ServerFrame.INSTANCE.updateClientMsg("客户端请求退出");
            Server.clients.remove(ctx.channel());
            ctx.close();
        }else{
            //把该消息发送给当前连接上的所有客户端
            Server.clients.writeAndFlush(buf);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //删除出现异常的客户端channel,并关闭连接
        Server.clients.remove(ctx.channel());
        ctx.close();
    }
}