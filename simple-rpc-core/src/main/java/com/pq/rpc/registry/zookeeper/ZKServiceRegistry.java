package com.pq.rpc.registry.zookeeper;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.RegistryConfig;
import com.pq.rpc.registry.api.ServiceAddOrUpdateCallback;
import com.pq.rpc.registry.api.ServiceOfflineCallback;
import com.pq.rpc.registry.api.ServiceURL;
import com.pq.rpc.registry.api.support.AbstractServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * 基于zookeeper实现的服务注册中心
 * zookeeper:一种分布式一致性的工业解决方案(分布式协调框架)
 * 可用于保证分布式一致性并提供分布式锁
 * 通过{数据结构(Znode)+原语+watcher机制}实现的
 *
 * @author pengqi
 * create at 2019/6/29
 */
@Slf4j
public class ZKServiceRegistry extends AbstractServiceRegistry {

    private ZKSupport zkSupport;    //ZKSupport变量,用于操作Zookeeper客户端

    private static final String ZK_REGISTRY_PATH = "/srpc";

    private volatile Thread discoveringThread;

    private static final long PARK_TIME = 1000000000L;

    public ZKServiceRegistry(RegistryConfig registryConfig){
        this.registryConfig = registryConfig;
    }

    @Override
    public void init() {
        zkSupport = new ZKSupport();
        zkSupport.connect(registryConfig.getAddress());     //连接zookeeper服务器
    }

    /**
     * 服务发现,用于consumer
     *
     * @param interfaceName              服务接口名
     * @param serviceOfflineCallback     服务下线回调函数
     * @param serviceAddOrUpdateCallback 服务上线/更新回调函数
     */
    @Override
    public void discover(String interfaceName, ServiceOfflineCallback serviceOfflineCallback, ServiceAddOrUpdateCallback serviceAddOrUpdateCallback) {
        log.info("discovering...");
        discoveringThread = Thread.currentThread();
        watchInterface(interfaceName,serviceOfflineCallback,serviceAddOrUpdateCallback);    //发现服务并注册监听
        LockSupport.parkNanos(this,PARK_TIME);  //阻塞线程,直到服务发现完成为止,防止ClusterInvoker中的map还未初始化就被使用
    }

    /**
     * 监听指定服务接口在ZK中的状态
     */
    private void watchInterface(String interfaceName,ServiceOfflineCallback serviceOfflineCallback,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback){
        String path = generatePath(interfaceName);  //生成对应服务接口在ZK中的路径
        try{
            //获取子节点并注册监听事件
            List<String> addresses = zkSupport.getChildren(path,watchedEvent -> {
                if(watchedEvent.getType()== Watcher.Event.EventType.NodeChildrenChanged){
                    //如果事件为子节点变更事件,则再次获取子节点
                    watchInterface(interfaceName,serviceOfflineCallback,serviceAddOrUpdateCallback);
                }
            });
            List<ServiceURL> dataList = new ArrayList<>();
            for(String node:addresses){
                dataList.add(watchService(interfaceName,node,serviceAddOrUpdateCallback));
            }
            //回调,更新客户端clusterInvoker维护的注册服务映射表
            serviceOfflineCallback.removeNotExisted(dataList);
            LockSupport.unpark(discoveringThread);  //唤醒服务发现的线程
        }catch (KeeperException | InterruptedException e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.REGISTRY_ERROR,"ZK故障");
        }
    }

    private ServiceURL watchService(String interfaceName,String address,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback){
        String path = generatePath(interfaceName);
        try{
            //获取节点数据并注册监听事件
            byte[] bytes = zkSupport.getData(path+"/"+address,watchedEvent -> {
                if(watchedEvent.getType()== Watcher.Event.EventType.NodeDataChanged){
                    //如果事件为节点数据变更事件,则再次获取该节点的最新数据
                    watchService(interfaceName,address,serviceAddOrUpdateCallback);
                }
            });
            //将节点数据解析成ServiceURL对象
            ServiceURL serviceURL = ServiceURL.parse(new String(bytes, Charset.forName("UTF-8")));
            //回调,更新客户端clusterInvoker维护的注册服务映射表
            serviceAddOrUpdateCallback.addOrUpdate(serviceURL);
            return serviceURL;
        }catch (KeeperException | InterruptedException e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.REGISTRY_ERROR,"ZK故障");
        }
    }

    /**
     * 服务注册方法,用于provider
     *
     * @param serviceAddress 　服务提供方地址
     * @param interfaceName  服务名
     * @param interfaceClass 服务接口Class对象
     */
    @Override
    public void register(String serviceAddress, String interfaceName, Class<?> interfaceClass) {
        String path = generatePath(interfaceName);
        //先创建路径
        try{
            zkSupport.createPathIfAbsent(path, CreateMode.PERSISTENT);
        }catch (KeeperException | InterruptedException e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.REGISTRY_ERROR,"ZK故障");
        }
        //后创建节点
        zkSupport.createNodeIfAbsent(serviceAddress,path);
    }

    @Override
    public void close() {
        if(zkSupport!=null){
            zkSupport.close();
        }
    }

    /**
     * 根据服务接口名生成ZK路径名
     */
    private String generatePath(String interfaceName){
        return ZK_REGISTRY_PATH + "/" + interfaceName;
    }
}
