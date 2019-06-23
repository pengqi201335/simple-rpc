package com.pq.rpc.config;

import com.pq.rpc.registry.api.ServiceRegistry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册中心配置类，配置以下属性
 * 1)注册中心类型
 * 2)注册中心地址
 * 3)注册中心实例
 *
 * @author pengqi
 * create at 2019/6/20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryConfig {

    private String type;

    private String address;

    private ServiceRegistry serviceRegistryInstance;
}
