package com.pq.rpc.protocol.support;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.registry.api.ServiceURL;
import com.pq.rpc.transport.api.Client;
import com.pq.rpc.transport.api.Server;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的远程协议对象
 * <p>
 * 负责具体远程协议的辅助工作
 * 从这一层开始涉及到底层运输层的东西,之所以在这里管理Client连接,是因为connection是address维度的
 * 即两个address之间只需要一条连接,而与他们之间有多少调用关系无关
 * 一条连接上的endPoints中的invokers共享这条连接
 *</p>
 *
 * @author pengqi
 * create at 2019/6/23
 */
@Slf4j
public abstract class AbstractRemoteProtocol extends AbstractProtocol {
    /**
     * key:address
     * value:Client
     * 一个address对应一个client连接
     * 一条连接上的两个endPoint不论invoker是否相同,都共享此连接,避免无意义地重复创建
     */
    private Map<String, Client> clientMap = new ConcurrentHashMap<>();

    /**
     * 锁map
     * 以address创建锁对象,同一时刻两个目标address相同的protocol只能有一个获取该锁对象并进入临界区
     */
    private Map<String,Object> locks = new ConcurrentHashMap<>();

    /**
     * 服务端连接
     */
    private Server server;

    /**
     * 初始化protocol对应的客户端并返回
     * @return 经初始化的通信客户端
     */
    protected final Client initClient(ServiceURL serviceURL){
        //目标服务器address
        String address = serviceURL.getServiceAddress();
        locks.putIfAbsent(address,new Object());    //创建锁对象
        synchronized (locks.get(address)){
            //本机已经和目标服务器建立连接
            if(clientMap.containsKey(address)){
                return clientMap.get(address);
            }
            //本机还未与目标服务器建立连接
            Client client = doInitClient(serviceURL);
            clientMap.put(address,client);
            return client;
        }
    }

    /**
     * 抽象方法:目标服务器并未与本机进行连接时,由具体的protocol实例来创建连接并返回客户端
     * @param serviceURL 包含目标服务信息的ServiceURL
     * @return 客户端
     */
    protected abstract Client doInitClient(ServiceURL serviceURL);

    /**
     * 更新端点的配置
     * @param serviceURL 新的配置信息
     */
    public final void updateEndpointConfig(ServiceURL serviceURL){
        if(!clientMap.containsKey(serviceURL.getServiceAddress())){
            throw new RPCException(ExceptionEnum.SERVER_ADDRESS_IS_NOT_CONFIGURATION,"SERVER_ADDRESS_IS_NOT_CONFIGURATION");
        }
        //调用transport层的接口更新端点配置
        clientMap.get(serviceURL.getServiceAddress()).updateServiceConfig(serviceURL);
    }

    public final void closeEndpoint(String address){
        Client client = clientMap.remove(address);

        if(client!=null){
            log.info("正在关闭客户端..."+address);
            client.close();
        }else{
            log.error("请勿重复关闭客户端..."+address);
        }
    }

    /**
     * 开启服务端连接
     */
    protected synchronized final void openServer(){
        if(server==null){
            doOpenServer();
        }
    }

    protected abstract void doOpenServer();

    @Override
    public void close() {
        clientMap.values().forEach(Client::close);
        if(server!=null)
            server.close();
    }
}
