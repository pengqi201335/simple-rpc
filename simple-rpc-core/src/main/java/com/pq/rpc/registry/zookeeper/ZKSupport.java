package com.pq.rpc.registry.zookeeper;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.exception.RPCException;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper注册中心的支持类,提供以下功能:
 * 1)连接注册中心服务器
 * 2)创建Znode节点
 * 3)创建ZK路径
 * 4)关闭与zookeeper服务器的连接
 *
 * @author pengqi
 * create at 2019/6/29
 */
@Slf4j
public class ZKSupport {

    //ZK变量
    protected ZooKeeper zooKeeper = null;

    private volatile CountDownLatch connectedSemaphore = new CountDownLatch(1);

    private static final int ZK_SESSION_TIMEOUT = 5000;

    /**
     * 初始化客户端Zookeeper变量,连接远程zookeeper服务器
     */
    public void connect(String address){
        try{
            this.zooKeeper = new ZooKeeper(address,ZK_SESSION_TIMEOUT,watchedEvent -> {
                Watcher.Event.KeeperState keeperState = watchedEvent.getState();
                Watcher.Event.EventType eventType = watchedEvent.getType();
                if(keeperState==Watcher.Event.KeeperState.SyncConnected){
                    //同步连接成功事件
                    if(eventType==Watcher.Event.EventType.None){
                        connectedSemaphore.countDown();
                        log.info("成功连接ZK服务器");
                    }
                }
            });
            connectedSemaphore.await();     //阻塞当前线程,直到连接ZK服务器成功
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据zk路径和数据创建zk节点
     * @param data 节点中的数据
     * @param path 节点路径
     */
    public void createNodeIfAbsent(String data,String path){
        try{
            byte[] bytes = data.getBytes(Charset.forName("UTF-8"));     //节点数据
            zooKeeper.create(path,bytes,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL);  //创建一个临时节点
            log.info("成功创建一个zk节点({}—>{})",path,data);
        }catch (KeeperException e){
            if(e instanceof KeeperException.NodeExistsException){
                //该路径下已经存在这样一个节点
                throw new RPCException(ExceptionEnum.REGISTRY_ERROR,"ZK路径"+path+"下已存在节点"+data);
            }else{
                e.printStackTrace();
            }
        }catch (InterruptedException e){
            e.getCause();
        }
    }

    /**
     * 创建路径,路径中的节点不存放数据
     * @param path 路径
     * @param createMode 路径中的节点类型(为持久节点)
     */
    public void createPathIfAbsent(String path, CreateMode createMode) throws KeeperException,InterruptedException {
        String[] split = path.split("/");
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<split.length;i++){
            if(StringUtils.hasText(split[i])){
                sb.append(split[i]);
                Stat stat = zooKeeper.exists(sb.toString(),false);  //当前节点状态
                if(stat==null){
                    //当前路径不存在,创建当前节点,此节点不存放数据,仅作为路径使用
                    zooKeeper.create(sb.toString(),new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,createMode);
                }
            }
            if(i<split.length-1){
                sb.append("/");
            }
        }
    }

    /**
     * 获取某个路径下的所有Znode子节点
     * @param path 路径
     * @param watcher 监听器
     * @return 子节点列表
     */
    public List<String> getChildren(String path,Watcher watcher)
    throws KeeperException,InterruptedException{
        return zooKeeper.getChildren(path,watcher);
    }

    /**
     * 获取某个节点字节数组形式的数据
     * @param path 节点路径
     * @param watcher 监听器
     * @return 字节数组
     */
    public byte[] getData(String path,Watcher watcher)
    throws KeeperException,InterruptedException{
        return zooKeeper.getData(path,watcher,null);
    }

    /**
     * 关闭zookeeper连接
     */
    public void close(){
        try{
            this.zooKeeper.close();
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
