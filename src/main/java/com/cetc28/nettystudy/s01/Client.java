package com.cetc28.nettystudy.s01;

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
 * @Date: 2022/1/27 - 01 - 27 - 15:18
 * @Description: com.cetc28.nettystudy.s01
 * @version: 1.0
 */
public class Client {
    // netty客户端
    public static void main(String[] args) throws InterruptedException {
        //循环处理线程池: 用来处理整个channel上的所有事件(网络上的那些事件) 比如要连接:出一个线程来干; 要读取,出一个线程来干;
        EventLoopGroup group = new NioEventLoopGroup(1);//默认值: 线程非常多(核数*2); 客户端来讲,1个线程就够
        //nettySocketChannel的辅助启动类: 靴子带
        Bootstrap b = new Bootstrap();
        try {
            //连接是否成功的反馈是在ChannelFuture中
            ChannelFuture f = b.group(group)//把上面那个线程池传进来:以后任何事件(连接/读写)都由这个线程池中的空线程来处理
                    .channel(NioSocketChannel.class)//指定我们将来连到服务器上的channel类型(SocketChannel:BIO阻塞版)
                    .handler(new ClientChannelInitializer())//channel做初始化用的一个类
                    .connect("localhost", 8888);//netty中所有方法都是异步方法:调用完马上往下运行,不知道成功与否
//                    .sync();//使当前的方法从异步->同步: 必须等这个方法执行完再执行后面的方法

            //对于异步方法,连接到底成功不成功,得写个监听器去监听它
            f.addListener(new ChannelFutureListener() {
                @Override//一旦整个调用结束后,f里有结果了,就会调用这个
                public void operationComplete(ChannelFuture future) throws Exception {
                    if(!future.isSuccess()){
                        System.out.println("没有连接成功");
                    }else{
                        System.out.println("连接成功");
                    }
                }
            });
            f.sync();//让监听器阻塞住, 等连接出了结果为止, 然后再往下执行, 不然加了监听器之后程序可能就结束啦!
            System.out.println("...");
            f.channel().closeFuture().sync();//close()->ChannelFuture: 只有client调用close方法,这句代码才会继续往下执行,因此程序阻塞
        }finally {
            group.shutdownGracefully();
        }
    }
}

class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{//指定网络的类型: 网络连接
    //当我们的channel进行初始化后,调用initChannel
    @Override//试验得出: 调用connect方法之后才会调用channel初始化这个函数;
    protected void initChannel(SocketChannel ch) throws Exception {
        //注: Netty是基于事件类型的, 我们只需写事件发生后的处理代码就可以, 用起来很方便
        System.out.println("channel初始化:");
        System.out.println(ch);

        ChannelPipeline pl = ch.pipeline();
        pl.addLast(new ClientHandler());
    }
}

class ClientHandler extends ChannelInboundHandlerAdapter{
    @Override//只要当前这个channel能用的时候,就调用这个函数
    public void channelActive(ChannelHandlerContext ctx) throws Exception {//channel 第一次连上可用,写出一个字符串
        //java的NIO自己的ByteBuffer很难用,因此netty实现了自己的ByteBuf用来传输数据,在netty中传数据,最终都要靠ByteBuf来传输数据
        //ByteBuf效率特别高,因为是Direct Memory,直接访问os内存,(缺点:跳过了java的垃圾回收机制,需要手动释放)
        ByteBuf buf = Unpooled.copiedBuffer("hello".getBytes());
        ctx.writeAndFlush(buf);//writeAndFlush这个方法: write完之后,自动释放buf
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = null;
        try {
            buf = (ByteBuf)msg;
            //将buf从ByteBuf转换成Java中的字节数组
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(),bytes);
            //将字节数组转换成java中的String打印输出
            System.out.println(new String(bytes));
        }finally {
            if (buf != null){
                ReferenceCountUtil.release(buf);//我们要手动释放buf的内存,因为它是DirectMemory(直指向OS内存)
//                System.out.println("buf引用数: " + buf.refCnt());//看一下buf有多少个引用指向它
            }
        }
    }
}