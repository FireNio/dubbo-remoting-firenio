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

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.ChannelEventListener;
import com.firenio.baseio.component.NioSocketChannel;

/**
 * BaseIOServer
 * 
 * @author qian.lei
 * @author william.liangf
 * @author ding.lid
 */
public class BaseioServer extends AbstractServer {

	private ChannelAcceptor			context;
	private Map<InetSocketAddress, Channel> channels = new ConcurrentHashMap<>();

	public BaseioServer(URL url, ChannelHandler handler) throws RemotingException {
		super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
	}

	@Override
	protected void doOpen() throws Throwable {
		// set thread pool.
		context = new ChannelAcceptor(getBindAddress().getPort());
		context.setProtocolCodec(new BaseioCodec(getCodec(), getUrl(), this));
		context.setIoEventHandle(new BaseioHandler(this));
		context.addChannelEventListener(new ChannelManager());
		context.bind();
	}

	@Override
	protected void doClose() throws Throwable {
		Util.unbind(context);
	}

	public Collection<Channel> getChannels() {
		return channels.values();
	}

	public Channel getChannel(InetSocketAddress remoteAddress) {
		return channels.get(remoteAddress);
	}

	public boolean isBound() {
		return context.isActive();
	}
	
	class ChannelManager implements ChannelEventListener{

        @Override
        public void channelOpened(NioSocketChannel ch) throws Exception {
            channels.put(new InetSocketAddress(ch.getRemoteAddr(), ch.getRemotePort()), BaseioChannel.getOrAddChannel(ch, getCodec(), getUrl(), getDelegateHandler()));
        }

        @Override
        public void channelClosed(NioSocketChannel ch) {
            channels.remove(new InetSocketAddress(ch.getRemoteAddr(), ch.getRemotePort()));
            BaseioChannel.removeChannelIfDisconnectd(ch);
        }
	    
	    
	}

}