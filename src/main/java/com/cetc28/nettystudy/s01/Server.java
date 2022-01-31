package com.cetc28.nettystudy.s01;

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

/**
 * @Auther: WSC
 * @Date: 2022/1/30 - 01 - 30 - 21:20
 * @Description: com.cetc28.nettystudy.s01
 * @version: 1.0
 */
public class Server {
    //用一个默认的线程来处理通道组上那些事件: 这个通道组保存所有已经连接上的客户端通道
    public static ChannelGroup clients = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 这是程序的main函数:入口函数
    public static void main(String[] args){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);//只负责连接
        EventLoopGroup workGroup = new NioEventLoopGroup(1);//负责连接之后的读写操作

        ServerBootstrap b = new ServerBootstrap();
        try {
            //产生一个channelFuture, 可以加监听器看是否初始化成功, 一般都会成功, 所以这里不加监听器
            ChannelFuture f = b.group(bossGroup,workGroup)//一个用来处理连接, 一个用来处理读写
                    .channel(NioServerSocketChannel.class)//channel类型
                    .childHandler(new ChannelInitializer<SocketChannel>() {//childHandler: 每个连接的channel建立后执行
                        @Override//channel初始化完成之后执行, 因为是childHandler(不负责连接)
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println(ch);//把当前连接的channel信息打印
                            //System.out.println(Thread.currentThread().getId());//把当前线程的id打印

                            // 要想在server端处理数据,我们先拿到和client连接的这个channel,调用其pipeline方法,
                            // 返回值是一个ChannelPipeline(管道),里面可以放很多Inbound Handler来处理读入数据,
                            // 也可以放很多Outbound Handler来处理写出数据,
                            // 责任链模式: ChannelPipeline上面可以放很多的责任处理器
                            ChannelPipeline pl = ch.pipeline();
                            pl.addLast(new ServerChildHandler());
                        }
                    })
                    .bind(8888)//将server绑定到8888端口,开始监听
                    .sync();//使得bind执行完, 再继续执行下去(否则都不知道有没有bind成功就打印"服务器启动成功!")

            System.out.println("服务器启动成功!");

            //如果没这句话, 监听好了, 程序也结束了;
            f.channel().closeFuture().sync();//close()->ChannelFuture: 只有server调用close方法,这句代码才会继续往下执行,因此程序阻塞
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}

//追根溯源 ServerChildHandler 最终会继承到 ChannelHandler, 如果直接implement channelHandler, 会有很多方法要实现(麻烦)
class ServerChildHandler extends ChannelInboundHandlerAdapter{
    @Override//当这个channel可以使用的时候就可以调用这个函数
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println(Thread.currentThread().getId());//把当前线程的id打印
        Server.clients.add(ctx.channel());//将连接好的通道保存到通道组里
    }

    @Override//当前channel有数据发过来时(client往server发数据), 会调用这个方法, 数据保存在msg中, msg格式:ByteBuf;
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf)msg;
            //将buf从ByteBuf转换成Java中的字节数组
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            //将字节数组转换成String打印输出
            System.out.println(new String(bytes));

            //顺着这个channel把收到的Buf重新返回到client
//            ctx.writeAndFlush(buf);//writeAndFlush: 调用完之后自动释放buf内存, 因此不用finally中手动接着处理
            //将收到的消息发给通道组中所有的channel
            Server.clients.writeAndFlush(buf);
        }finally {
//            if (buf != null){
//                ReferenceCountUtil.release(buf);//我们要手动释放buf的内存,因为它是DirectMemory(直指向OS内存)
////                System.out.println(buf.refCnt());//看一下buf有多少个引用指向它
//            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //当出现异常时, 打印异常
        cause.printStackTrace();
        //然后将当前通道关闭, 所以对应的客户端f.channel().closeFuture().sync()代码继续往后执行,客户端也关闭
        ctx.close();
    }
}