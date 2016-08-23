/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.remoting.transport.baseio;

import java.io.IOException;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffer;
import com.alibaba.dubbo.remoting.buffer.ChannelBuffers;
import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.buffer.ByteBufUtil;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.protocol.Frame;
import com.firenio.baseio.protocol.ProtocolCodec;

final class BaseioCodec extends ProtocolCodec {

    public static final int      P_HEADER_LENGTH = 16;
    private final Codec2         codec;
    private final URL            url;
    private final ChannelHandler handler;

    public BaseioCodec(Codec2 codec, URL url, ChannelHandler handler) {
        this.codec = codec;
        this.url = url;
        this.handler = handler;
    }

    @Override
    public Frame decode(NioSocketChannel ch, ByteBuf src) throws IOException {
        ChannelBuffer message = ChannelBuffers.wrappedBuffer(src.nioBuffer());
        BaseioChannel channel = BaseioChannel.getOrAddChannel(ch, codec, url, handler);
        Object msg;
        int saveReaderIndex;
        try {
            // decode object.
            do {
                saveReaderIndex = message.readerIndex();
                try {
                    msg = codec.decode(channel, message);
                } catch (IOException e) {
                    throw e;
                }
                if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                    message.readerIndex(saveReaderIndex);
                    break;
                } else {
                    //is it possible to go here ?
                    if (saveReaderIndex == message.readerIndex()) {
                        throw new IOException("Decode without read data.");
                    }
                    if (msg != null) {
                        src.position(message.readerIndex());
                        return new DubboFrame(msg);
                    }
                }
            } while (message.readable());
            src.position(message.readerIndex());
            return null;
        } finally {
            BaseioChannel.removeChannelIfDisconnectd(ch);
        }
    }

    @Override
    public ByteBuf encode(NioSocketChannel ch, Frame frame) throws IOException {
        DubboFrame f = (DubboFrame) frame;
        BaseioChannel channel = BaseioChannel.getOrAddChannel(ch, codec, url, handler);
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(1024);
        try {
            codec.encode(channel, buffer, f.getMsg());
        } finally {
            BaseioChannel.removeChannelIfDisconnectd(channel.getSession());
        }
        return ByteBufUtil.wrap(buffer.toByteBuffer());
    }

    public String getProtocolId() {
        return "Dubbo";
    }

}
