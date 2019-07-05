package com.pq.rpc.autoconfig;

import com.pq.rpc.config.ApplicationConfig;
import com.pq.rpc.config.ClusterConfig;
import com.pq.rpc.config.ProtocolConfig;
import com.pq.rpc.config.RegistryConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 基于properties配置文件的方式配置bean
 */
@ConfigurationProperties(prefix = "rpc")
@Data
public class RPCProperties {

    private ApplicationConfig applicationConfig;

    private ProtocolConfig protocolConfig;

    private ClusterConfig clusterConfig;

    private RegistryConfig registryConfig;
}
