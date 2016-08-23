package com.alibaba.dubbo.remoting.transport.baseio;

import com.firenio.baseio.protocol.AbstractFrame;

public class DubboFrame extends AbstractFrame {

    private Object msg;

    public DubboFrame(Object msg) {
        this.msg = msg;
    }

    public Object getMsg() {
        return msg;
    }

}
