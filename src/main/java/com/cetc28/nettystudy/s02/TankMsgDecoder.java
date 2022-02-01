package com.cetc28.nettystudy.s02;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @Auther: WSC
 * @Date: 2022/2/1 - 02 - 01 - 18:48
 * @Description: com.cetc28.nettystudy.s02
 * @version: 1.0
 */
public class TankMsgDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //写过来至少8个字节, 因此必须读完8个字节在处理
        if(in.readableBytes() < 8){//TCP 拆包 粘包
            return;
        }

//        in.markReaderIndex();

        int x = in.readInt();
        int y = in.readInt();

        out.add(new TankMsg(x,y));
    }
}
