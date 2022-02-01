package com.cetc28.nettystudy.s02;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;

/**
 * @Auther: WSC
 * @Date: 2022/1/31 - 01 - 31 - 21:25
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class Client {

    //client端保存一个channel用来发送消息
    private Channel channel = null;

    public void connect(){
        EventLoopGroup group = new NioEventLoopGroup(1);
        Bootstrap b = new Bootstrap();

        try {
            ChannelFuture cf = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitializer())
                    .connect("localhost", 8888);

            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("连接成功!");
                        System.out.println("operationComplete监听器监听连接成功后的future中的channel: " + future.channel());
                        channel = future.channel();//客户端连接成功后即初始化
                    }else{
                        System.out.println("连接失败!");
                    }
                }
            });

            cf.sync();
            System.out.println("...");
            cf.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public void send(String msg){
        ByteBuf buf = Unpooled.copiedBuffer(msg.getBytes());
        channel.writeAndFlush(buf);
    }

    public void closeConnect(){
        this.send("_bye_");
    }

    // 这是程序的main函数:入口函数
    public static void main(String[] args) {
        new Client().connect();
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        System.out.println("initChannel的SocketChannel: " + ch);
        //添加pipeline(责任链模式)
        ch.pipeline().addLast(new ClientHandler());
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter{
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive中的channel: " + ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf)msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            System.out.println(new String(bytes));
            ClientFrame.INSTANCE.updateContext(new String(bytes));
        }finally {
            if(buf != null){
                ReferenceCountUtil.release(buf);
            }
        }
    }
}