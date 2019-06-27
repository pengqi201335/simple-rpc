package com.pq.rpc.cluster.support;

import com.pq.rpc.common.context.RPCThreadLocalContext;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import com.pq.rpc.config.GlobalConfig;
import com.pq.rpc.config.ReferenceConfig;
import com.pq.rpc.protocol.api.InvokeParam;
import com.pq.rpc.protocol.api.Invoker;
import com.pq.rpc.protocol.api.support.AbstractRemoteProtocol;
import com.pq.rpc.registry.api.ServiceURL;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 集群层面的Invoker,负责负载均衡和集群容错
 * 一个clusterInvoker对应一个服务的集群,持有cluster层的其他组件,如LoadBalancer和FaultToleranceHandler
 *
 * @author pengqi
 * create at 2019/6/21
 */
@Slf4j
public class ClusterInvoker<T> implements Invoker<T> {

    /**
     * 服务调用方维护的服务提供者列表
     * key为服务器address,value为一个protocol层的invoker
     *
     */
    private Map<String, Invoker<T>> addressInvokers = new ConcurrentHashMap<>();

    private String interfaceName;

    private Class<T> interfaceClass;

    private GlobalConfig globalConfig;

    public ClusterInvoker(String interfaceName,Class<T> interfaceClass,GlobalConfig globalConfig){
        this.interfaceClass = interfaceClass;
        this.interfaceName = interfaceName;
        this.globalConfig = globalConfig;
        init();
    }

    @Override
    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * 将addressInvokers中的服务提供者集合转换为列表并返回
     * @return 可用服务列表
     */
    private List<Invoker> getAvailableInvokers(){
        return new ArrayList<>(addressInvokers.values());
    }

    /**
     * 调用服务，返回调用结果
     *
     * @param invokeParam 参数
     * @return RPCResponse对象
     */
    @Override
    public RPCResponse invoke(InvokeParam invokeParam) {
        // 选择一个服务(注意:此时的调用者invoker是protocol层面的)
        Invoker protocolInvoker = doSelect(getAvailableInvokers(),invokeParam);
        // 将该invoker设置为当前线程的Invoker对象，这样就保证了每个线程对应一个独立的invoker
        RPCThreadLocalContext.getContext().setInvoker(protocolInvoker);

        try{
            //调用服务,返回结果response
            RPCResponse response = protocolInvoker.invoke(invokeParam);
            if(response==null){
                //当调用方式为oneWay/future/callback时,调用结果是为null的
                //异步调用时,完成之后要去RPCThreadSharedContext里取future对象来get()最后的结果
                return null;
            }

            //如果调用过程出现错误,抛出异常
            if(response.hasError()){
                //TODO recycle
                throw new RPCException(response.getErrorCause(),
                        ExceptionEnum.REMOTE_SERVICE_INVOCATION_FAILED,
                        "REMOTE_SERVICE_INVOCATION_FAILED");
            }

            //调用成功
            return response;
        }catch (RPCException e){
            //调用过程发生异常,启用集群容错机制
            return globalConfig.getClusterConfig().getFaultToleranceHandlerInstance().handler(this,invokeParam,e);
        }
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    /**
     * 初始化ClusterInvoker,主要是做了服务发现的工作
     * 连接注册中心,注册监听事件,一旦有服务上线/更新或下线,可以通过传入的两个回调函数进行回调,更新服务提供者列表
     *
     */
    private void init(){
        globalConfig.getRegistryConfig().getServiceRegistryInstance()
                .discover(interfaceName,
                          this::removeNotExisted,
                          this::addOrUpdate);
    }

    /**
     * 服务下线真正的回调函数
     * 将最新返回的serviceURL列表与之前的列表对比，删除已经不在新列表中的provider
     *
     * 添加synchronized关键字的原因:引用相同服务的调用者共享一个ClusterInvoker,可能会同时回调此函数,需要做同步处理
     *
     * @param serviceURLS 新返回的服务URL列表
     */
    private synchronized void removeNotExisted(List<ServiceURL> serviceURLS){
        //将serviceURL列表转换为address——>serviceURL的map
        Map<String,ServiceURL> newAddressServiceURLS = serviceURLS.stream().collect(Collectors.toMap(
                ServiceURL::getServiceAddress,
                url->url));

        Iterator<Map.Entry<String,Invoker<T>>> iter = addressInvokers.entrySet().iterator();
        //遍历当前可用服务列表,若没有出现在新列表中,则删除此服务,并断开与服务终端的连接
        while (iter.hasNext()){
            Map.Entry<String,Invoker<T>> cur = iter.next();
            if(!newAddressServiceURLS.containsKey(cur.getKey())){
                //当前服务已下线,删除对应的服务提供者
                log.info("PROVIDER:"+cur.getKey()+"IS ALREADY OFFLINE");
                //TODO 断开与对应服务器终端的连接

                //移除对应的服务provider
                iter.remove();
            }
        }
    }

    /**
     * 服务上线/更新真正的回调函数
     * 将serviceURL添加到addressInvokers集合中,或更新serviceURL对应的元素
     *
     * @param serviceURL 新添加或更新的serviceURL
     */
    @SuppressWarnings("unchecked")
    private synchronized void addOrUpdate(ServiceURL serviceURL){
        String address = serviceURL.getServiceAddress();    //发生变更的服务地址
        if(addressInvokers.containsKey(address)){
            //服务地址没有发生变化,只是配置被改变
            //只需要在protocol层对配置进行更新
            if(globalConfig.getProtocol() instanceof AbstractRemoteProtocol){
                AbstractRemoteProtocol protocol = (AbstractRemoteProtocol)globalConfig.getProtocol();
                //更新底层配置信息
                protocol.updateEndpointConfig(serviceURL);
            }
        }else{
            //新增服务地址
            log.info("NEW SERVER ADDED,INTERFACE_NAME:"+interfaceName+"NEW_SERVICE_URL:"+serviceURL);
            //引用远程服务,获得protocol层的Invoker对象
            //通过接口名来获取对应的ReferenceConfig对象,因为引用同一个服务的用户共享同一个ReferenceConfig
            Invoker protocolInvoker = globalConfig.getProtocolConfig().getProtocolInstance().refer(serviceURL,
                    ReferenceConfig.getReferenceConfigByInterfaceName(interfaceName));
            addressInvokers.put(serviceURL.getServiceAddress(),protocolInvoker);
        }
    }

    /**
     * 负载均衡,在服务提供者列表中选择一个
     * @param availableInvokers 可用的服务抽象调用者列表
     * @param invokeParam 服务调用的参数对象
     * @return 被负载均衡选中的invoker
     */
    private Invoker doSelect(List<Invoker> availableInvokers,InvokeParam invokeParam){
        if(availableInvokers.size()==0){
            //服务发现时没有找到可用的服务
            log.error("没有找到可用的服务");
            throw new RPCException(ExceptionEnum.NO_SERVER_FOUND,"NO AVAILABLE SERVER NOW");
        }
        Invoker invoker;
        if(availableInvokers.size()==1){
            //仅有一个服务可用
            invoker = availableInvokers.get(0);
            if(invoker.isAvailable()){
                return invoker;
            }else{
                log.error("找到一个服务，但该服务不可用");
                throw new RPCException(ExceptionEnum.NO_SERVER_FOUND,"NO AVAILABLE SERVER NOW");
            }
        }else{
            //找到多个服务实现
            //TODO add invokerParam
            invoker = globalConfig.getClusterConfig().getLoadBalancerInstance().select(availableInvokers);
            if(invoker.isAvailable()){
                return invoker;
            }else{
                availableInvokers.remove(invoker);  //被负载均衡策略选中的服务不可用，将其从可用服务列表中删除
                //重新选择
                return doSelect(availableInvokers,invokeParam);
            }
        }
    }

    /**
     * 调用失败后,重试时调用的invoke方法
     * 不捕获异常的原因是:会由调用此方法的函数来捕获异常
     *
     * @param availableInvokers 此时的可用服务列表
     * @param invokeParam 调用参数
     * @return 调用结果
     */
    public RPCResponse invokeForFaultTolerance(List<Invoker> availableInvokers,InvokeParam invokeParam){
        Invoker protocolInvoker = doSelect(availableInvokers,invokeParam);
        RPCThreadLocalContext.getContext().setInvoker(protocolInvoker);

        RPCResponse response = protocolInvoker.invoke(invokeParam);

        if(response==null){
            return null;
        }

        if(response.hasError()){
            throw new RPCException(response.getErrorCause(),
                    ExceptionEnum.REMOTE_SERVICE_INVOCATION_FAILED,
                    "REMOTE_SERVICE_INVOCATION_FAILED");
        }

        return response;
    }

    @Override
    public ServiceURL getServiceURL() {
        throw new UnsupportedOperationException();
    }
}
