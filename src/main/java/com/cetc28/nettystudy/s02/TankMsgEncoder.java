package com.cetc28.nettystudy.s02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @Auther: WSC
 * @Date: 2022/2/1 - 02 - 01 - 18:45
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class TankMsgEncoder extends MessageToByteEncoder<TankMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, TankMsg msg, ByteBuf buf) throws Exception {
        buf.writeInt(msg.x);
        buf.writeInt(msg.y);
    }
}
