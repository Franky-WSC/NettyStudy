package com.cetc28.nettystudy.s02Test;

import com.cetc28.nettystudy.s02.TankMsg;
import com.cetc28.nettystudy.s02.TankMsgDecoder;
import com.cetc28.nettystudy.s02.TankMsgEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Assert;
import org.junit.Test;

public class TankMsgCodecTest {
    @Test
    public void testTankMsgEncoder(){
        TankMsg msg = new TankMsg(10,10);
        //EmbeddedChannel: 嵌入式channel:用来测试编码解码用,方便
        EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder());//虚拟连到网上的一个channel,加入我们自定义的encoder
        ch.writeOutbound(msg);//通过这个channel向外写了一个msg(Tank对象)

        ByteBuf buf = (ByteBuf)ch.readOutbound();//通过这个channel读了一个msg对象(Tank对象)
        int x = buf.readInt();
        int y = buf.readInt();

        Assert.assertTrue(x == 10 && y == 10);
        buf.release();
    }

    @Test
    public void testTankMsgEncoder2(){
        //将TankMsg转换成一个ByteBuf
        ByteBuf buf = Unpooled.buffer();
        TankMsg msg = new TankMsg(10,10);
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
        //向嵌入式channel中加入两个处理器: 一个Encoder, 一个Decoder;
        EmbeddedChannel ch = new EmbeddedChannel(new TankMsgEncoder(),new TankMsgDecoder());
        //writeInbound: 从服务器端通过channel往客户端这边写ByteBuf
        ch.writeInbound(buf.duplicate());//ByteBuf经过Decoder变成TankMsg, 然后直接忽略Encoder

        TankMsg tm = (TankMsg)ch.readInbound();//会读到一个完整的TankMsg

        Assert.assertTrue(tm.x == 10 && tm.y == 10);
    }
}
