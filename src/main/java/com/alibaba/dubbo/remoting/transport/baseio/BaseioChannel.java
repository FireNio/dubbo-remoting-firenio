package com.alibaba.dubbo.remoting.transport.baseio;

import java.net.InetSocketAddress;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractChannel;
import com.firenio.baseio.component.ChannelContext;
import com.firenio.baseio.component.NioSocketChannel;

public class BaseioChannel extends AbstractChannel {
    
    static final String BASEIO_CHANNEL_KEY = "BASEIO_CHANNEL_KEY";

	static BaseioChannel getOrAddChannel(NioSocketChannel ch, Codec2 codec, URL url, ChannelHandler handler) {
		if (ch == null) {
			return null;
		}
		BaseioChannel channel = (BaseioChannel) ch.getAttribute(BASEIO_CHANNEL_KEY);
		if (channel == null) {
			synchronized (ch) {
			    channel = (BaseioChannel) ch.getAttribute(BASEIO_CHANNEL_KEY);
				if (channel != null) {
					return channel;
				}
				channel = new BaseioChannel(ch, codec, url, handler);
				ch.setAttribute(BASEIO_CHANNEL_KEY, channel);
				return channel;
			}
		}
		return channel;
	}

	static void removeChannelIfDisconnectd(NioSocketChannel ch) {
		if (ch != null && !ch.isOpened()) {
			ch.setAttribute(BASEIO_CHANNEL_KEY, null);
		}
	}

	private Codec2			codec;
	private NioSocketChannel	channel;
	private InetSocketAddress localAddress;
	private InetSocketAddress remoteAddress;

	private BaseioChannel(NioSocketChannel session, Codec2 codec, URL url, ChannelHandler handler) {
		super(url, handler);
		this.channel = session;
		this.codec = codec;
	}

	public Object getAttribute(String key) {
		return channel.getAttribute(key);
	}

	public Codec2 getCodec() {
		return codec;
	}

	public ChannelContext getContext() {
		return channel.getContext();
	}

	public InetSocketAddress getLocalAddress() {
	    if (localAddress == null) {
            localAddress = new InetSocketAddress(channel.getLocalAddr(), channel.getLocalPort()); 
        }
		return localAddress;
	}

	public InetSocketAddress getRemoteAddress() {
	    if (remoteAddress == null) {
	        remoteAddress = new InetSocketAddress(channel.getRemoteAddr(), channel.getRemotePort()); 
        }
        return remoteAddress;
	}

	public NioSocketChannel getSession() {
		return channel;
	}

	public boolean hasAttribute(String key) {
		return channel.getAttribute(key) != null;
	}

	public boolean isConnected() {
		return channel.isOpened();
	}

	public void removeAttribute(String key) {
		channel.removeAttribute(key);
	}

	@Override
	public void send(Object message, boolean sent) throws RemotingException {
		super.send(message, sent);
		if (message == null) {
			throw new IllegalArgumentException("empty message");
		}
		DubboFrame future = new DubboFrame(message);
		channel.flush(future);
	}

	public void setAttribute(String key, Object value) {
		channel.setAttribute(key, value);
	}

}
