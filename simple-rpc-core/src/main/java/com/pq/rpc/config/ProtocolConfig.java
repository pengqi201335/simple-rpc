package com.pq.rpc.config;

import com.pq.rpc.protocol.api.Protocol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 协议配置类，主要配置了以下属性
 * 1)协议类型
 * 2)端口号
 * 3)Protocol实例
 * 4)Executors实例
 * 5)默认端口8000
 *
 * @author pengqi
 * create at 2019/6/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolConfig {

    public static final Integer DEFAULT_PORT = 8000;

    private String type;

    private Integer port;

    private Protocol protocolInstance;

    private Executors executors;
}
