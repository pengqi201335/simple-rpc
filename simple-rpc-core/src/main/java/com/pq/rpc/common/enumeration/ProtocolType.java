package com.pq.rpc.common.enumeration;

import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.protocol.api.Protocol;
import com.pq.rpc.protocol.james.JamesProtocol;

/**
 * 协议枚举类
 * 目前只实现了James协议
 *
 * @author pengqi
 * create at 2019/7/3
 */
public enum ProtocolType implements ExtensionBaseType<Protocol> {
    JAMES(new JamesProtocol());     //James协议

    private Protocol protocol;

    ProtocolType(Protocol protocol){
        this.protocol = protocol;
    }

    @Override
    public Protocol getInstance() {
        return protocol;
    }
}
