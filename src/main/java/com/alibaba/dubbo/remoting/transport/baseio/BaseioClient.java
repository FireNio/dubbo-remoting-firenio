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
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;

/**
 * baseio client.
 * 
 * @author qian.lei
 * @author william.liangf
 */
public class BaseioClient extends AbstractClient {

    private static Logger    logger = LoggerFactory.getLogger(BaseioClient.class);
    private ChannelConnector context;

    public BaseioClient(final URL url, final ChannelHandler handler) throws RemotingException {
        super(url, wrapChannelHandler(url, handler));
    }

    @Override
    protected void doOpen() throws IOException {
        String hostName = getUrl().getHost();
        int port = getUrl().getPort();
        context = new ChannelConnector(hostName, port);
        context.setProtocolCodec(new BaseioCodec(getCodec(), getUrl(), this));
        context.setIoEventHandle(new BaseioHandler(this));
    }

    @Override
    protected void doConnect() throws IOException {
        context.connect();
    }

    @Override
    protected void doDisConnect() throws IOException {
        try {
            BaseioChannel.removeChannelIfDisconnectd(context.getChannel());
        } catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws IOException {
        Util.close(context);
    }

    @Override
    protected Channel getChannel() {
        return BaseioChannel.getOrAddChannel(context.getChannel(), getCodec(), getUrl(), this);
    }

}
