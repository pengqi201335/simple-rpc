package com.pq.rpc.common.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录当前请求积压情况的类,用于最小活跃度负载均衡,维护一个活跃度哈希表
 * 对于不同的RPC调用方式,活跃度的计算方式也不相同
 * 1)同步调用:发送请求前inc,自己get后dec
 * 2)异步调用:发送请求前inc,用户get后dec
 * 3)callback调用:发送请求前inc,收到服务端request后dec
 *
 * @author pengqi
 * create at 2019/7/1
 */
public class RPCStatus {

    /**
     * RPC请求活跃度映射表
     * k:由服务接口名+方法名+服务器IP地址生成的key
     * v:活跃度
     */
    private static final Map<String,Integer> RPC_ACTIVE_COUNT = new ConcurrentHashMap<>();

    /**
     * 获取活跃度
     */
    public synchronized static int getActivity(String interfaceName,String methodName,String address){
        Integer count =  RPC_ACTIVE_COUNT.get(generateKey(interfaceName,methodName,address));
        return count==null?0:count;
    }

    /**
     * 活跃度+1
     */
    public synchronized static void incActivity(String interfaceName, String methodName,String address){
        String key = generateKey(interfaceName,methodName,address);
        if(RPC_ACTIVE_COUNT.containsKey(key)){
            RPC_ACTIVE_COUNT.put(key,RPC_ACTIVE_COUNT.get(key)+1);
        }else{
            RPC_ACTIVE_COUNT.put(key,1);
        }
    }

    /**
     * 活跃度-1
     */
    public synchronized static void decActivity(String interfaceName,String methodName,String address){
        String key = generateKey(interfaceName,methodName,address);
        if(RPC_ACTIVE_COUNT.containsKey(key)){
            RPC_ACTIVE_COUNT.put(key,RPC_ACTIVE_COUNT.get(key)-1);
        }
    }

    /**
     * 根据服务接口名+方法名+地址生成一个key
     */
    private static String generateKey(String interfaceName,String methodName,String address){
        return interfaceName + "." + methodName + "." + address;
    }
}
