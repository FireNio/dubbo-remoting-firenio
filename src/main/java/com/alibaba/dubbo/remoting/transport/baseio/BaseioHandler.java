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

import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.NioSocketChannel;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;
import com.firenio.baseio.protocol.Frame;

/**
 * BaseIOHandler
 * 
 */
public class BaseioHandler extends IoEventHandle {

    private final ChannelHandler handler;
    private Logger               logger = LoggerFactory.getLogger(getClass());

    public BaseioHandler(ChannelHandler handler) {
        this.handler = handler;
    }

    public void accept(NioSocketChannel ch, Frame frame) throws Exception {
        DubboFrame f = (DubboFrame) frame;
        BaseioChannel channel = BaseioChannel.getOrAddChannel(ch, null, null, null);
        Object msg = f.getMsg();
        logger.debug("msg received:{}", msg);
        handler.received(channel, msg);
    }

    @Override
    public void exceptionCaught(NioSocketChannel ch, Frame future, Exception cause) {
        BaseioChannel channel = BaseioChannel.getOrAddChannel(ch, null, null, null);
        if (channel == null) {
            return;
        }
        try {
            handler.caught(channel, cause);
        } catch (RemotingException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
