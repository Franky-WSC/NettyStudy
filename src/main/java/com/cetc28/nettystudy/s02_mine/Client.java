package com.cetc28.nettystudy.s02_mine;

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
 * @Date: 2022/1/31 - 01 - 31 - 13:06
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class Client {
    EventLoopGroup group = null;
    Bootstrap b = null;
    public SocketChannel sc = null;
    public ClientFrame clientFrame;

    public Client(ClientFrame clientFrame) {
        this.clientFrame = clientFrame;

        group = new NioEventLoopGroup(1);
        b = new Bootstrap();

        try {
            ChannelFuture cf = b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ClientChannelInitialier(this))
                    .connect("localhost", 8888);
//                    .sync();

            cf.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(future.isSuccess()){
                        System.out.println("客户端连接成功!");
                    }else{
                        System.out.println("客户端连接失败!");
                    }
                }
            });

            cf.sync();
            System.out.println("...");
            //让Client阻塞,防止程序退出
            cf.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }

    public void sendMsgTOServer(String str){
        if(sc == null){
            return;
        }
        ByteBuf buf = Unpooled.copiedBuffer(str.getBytes());
        sc.writeAndFlush(buf);
    }
}

class ClientChannelInitialier extends ChannelInitializer<SocketChannel>{
    Client chatClient;

    public ClientChannelInitialier(Client chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        System.out.println("客户端初始化完毕;" + ch);
        //将当前的channel保存下来
        chatClient.sc = ch;
        //给当前连接进的channel添加责任处理器
        ChannelPipeline cp = ch.pipeline();
        cp.addLast(new MyClientHandler(chatClient));
    }
}

class MyClientHandler extends ChannelInboundHandlerAdapter{
    Client chatClient = null;

    public MyClientHandler(Client chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf) msg;
            //将服务器发来的消息在客户端中打印显示
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
//            System.out.println(new String(bytes));
            chatClient.clientFrame.showMsg(new String(bytes));
        }finally {
            if(buf != null){
                ReferenceCountUtil.release(buf);
            }
        }
    }
}
