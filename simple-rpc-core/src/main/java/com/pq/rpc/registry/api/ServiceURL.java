package com.pq.rpc.registry.api;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 服务URL
 * 服务是以URL的形式注册到注册中心的，该类表示一个服务URL，包含一个远程服务的所有信息
 *
 * @author pengqi
 * create at 2019/6/20
 */
@Slf4j
@EqualsAndHashCode(of = {"serviceAddress"})     //根据字段serviceAddress重写equals方法和hashCode方法(即serviceAddress相等equals就为true)
@ToString
public final class ServiceURL {

    private String serviceAddress;  //服务所在地址

    private Map<Key , List<String >> params = new HashMap<>();   //配置该服务的参数

    public static ServiceURL DEFAULT_SERVICE_URL;

    static {
        try{
            //默认URL为本机IP地址
            DEFAULT_SERVICE_URL = new ServiceURL(InetAddress.getLocalHost().getHostAddress());
        }catch (UnknownHostException e){
            e.printStackTrace();
        }
    }

    private ServiceURL(String serviceAddress){
        this.serviceAddress = serviceAddress;
    }

    private ServiceURL(){}

    public String getServiceAddress(){
        return serviceAddress;
    }

    public boolean containsKey(Key key){
        return params.containsKey(key);
    }

    /**
     * 根据参数的key返回对应的value,当参数没有配置时,也不能返回null,而是直接返回一个空列表
     * @param key 参数对应的key
     * @return 参数对应的值列表
     */
    public List<String> getParamsByKey(Key key){
        return containsKey(key)? params.get(key):key.getDefaultParams();
    }

    /**
     * 将字符串数据解析成对应的ServiceURL对象
     * @param data 待解析数据
     * @return 解析后的serviceURL
     */
    public static ServiceURL parse(String data){
        String[] url = data.split("\\?");

        ServiceURL serviceURL = new ServiceURL(url[0]);     //根据服务地址创建ServiceURL对象

        if(url.length > 1){
            //参数集合
            String params = url[1];
            String[] urlParams = params.split("&");
            for(String param : urlParams){
                //单独参数项
                String[] kv = param.split("=");
                String key = kv[0]; //参数名
                Key enumParamKey = Key.valueOf(key.toUpperCase());  //根据参数名在枚举类中找到对应的常量值作为参数key
                if(enumParamKey!=null){
                    String value = kv[1];
                    String[] values = value.split(",");
                    serviceURL.params.put(enumParamKey,Arrays.asList(values));
                }else{
                    log.error("NO SUCH KEY:"+key);
                }
            }
        }

        return serviceURL;
    }

    /**
     * 参数枚举类,目前只有权重这一项参数
     */
    public enum  Key{
        WEIGHT(Arrays.asList("100"));
        private List<String> defaultParams;

        Key(List<String> defaultParams){
            this.defaultParams = defaultParams;
        }

        @SuppressWarnings("unchecked")
        public List<String> getDefaultParams(){
            return defaultParams==null? Collections.EMPTY_LIST:defaultParams;
        }
    }
}
