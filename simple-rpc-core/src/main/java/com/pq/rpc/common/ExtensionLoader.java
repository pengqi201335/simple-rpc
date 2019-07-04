package com.pq.rpc.common;

import com.pq.rpc.common.enumeration.ExceptionEnum;
import com.pq.rpc.common.enumeration.support.ExtensionBaseType;
import com.pq.rpc.common.exception.RPCException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * 简易扩展点加载工具类
 * 应用内的依赖注入使用枚举单例的形式
 * 应用外的依赖注入使用Java的SPI机制
 *
 * @author pengqi
 * create at 2019/7/3
 */
@Slf4j
public class ExtensionLoader {

    private static ExtensionLoader INSTANCE = new ExtensionLoader();    //单例

    public static ExtensionLoader getINSTANCE(){
        return INSTANCE;
    }

    /**
     * 加载指定目录下的配置文件
     */
    public void loadResources(){
        URL parent = this.getClass().getClassLoader().getResource("/rpc");
        if(parent!=null){
            log.info("开始读取{/rpc}目录下的配置文件...");
            File dir = new File(parent.getFile());
            File[] files = dir.listFiles();
            for(File file:files){
                handleFile(file);
            }
            log.info("配置文件读取完成！");
        }
    }

    /**
     * 处理配置文件
     * 将配置文件中的实现类实例化并注册到extensionMap中
     * @param file 配置文件
     */
    private void handleFile(File file){
        log.info("开始读取配置文件:{}",file);
        String interfaceName = file.getName();      //配置文件名为接口的全限定名
        try{
            Class<?> interfaceClass = Class.forName(interfaceName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line=reader.readLine())!=null){
                String[] kv = line.split("=");
                if(kv.length!=2){
                    log.error("配置文件格式错误,应写成k=v形式");
                }
                //实例化实现类
                try{
                    Class<?> impl = Class.forName(kv[1]);
                    if(!interfaceClass.isAssignableFrom(impl)){
                        log.error("{}并非接口{}的实现类！",kv[1],interfaceName);
                        throw new RPCException(ExceptionEnum.EXTENSION_CONFIG_ERROR,"配置的类"+impl+"并非接口"+interfaceName+"的实现类");
                    }
                    Object o = impl.getDeclaredConstructor().newInstance();
                    registry(interfaceClass,kv[0],o);
                }catch (Throwable t){
                    t.printStackTrace();
                    throw new RPCException(ExceptionEnum.EXTENSION_CONFIG_ERROR,"加载类或实例化失败");
                }
            }
        }catch (IOException e){
            e.printStackTrace();
            throw new RPCException(ExceptionEnum.EXTENSION_CONFIG_ERROR,"配置文件读取失败");
        }catch (ClassNotFoundException e){
            e.getCause();
            throw new RPCException(ExceptionEnum.EXTENSION_CONFIG_ERROR,"未找到待配置实例的接口");
        }

    }

    private Map<String,Map<String,Object>> extensionMap = new HashMap<>();  //保存已加载的实现类的map,第一个String表示接口名
                                                                            //第二个String表示一个实现类的别名

    private ExtensionLoader(){}

    /**
     * 根据接口类型、枚举类型和实现类名来获取一个实现类的实例
     * @param interfaceClass 接口类类型
     * @param enumType 枚举类类型
     * @param type 实现类类名
     * @param <T> 泛型,服务类型
     * @return 服务接口实例
     */
    @SuppressWarnings("unchecked")
    public  <T> T load(Class<T> interfaceClass,Class enumType,String type){
        ExtensionBaseType<T> extensionBaseType = ExtensionBaseType.valueOf(enumType,type);
        //针对应用内的依赖
        if(extensionBaseType!=null){
            return extensionBaseType.getInstance();     //返回枚举单例中对应的实现类实例
        }
        //针对应用外的依赖
        if(!extensionMap.containsKey(interfaceClass.getName())){
            throw new RPCException(ExceptionEnum.NO_SUPPORTED_INSTANCE,interfaceClass+"没有可用的实现类");
        }
        //依赖注入之前,已经读取完配置文件并将实现类实例注册在map中,此时只需要从map中取实例
        Object o = extensionMap.get(interfaceClass.getName()).get(type);
        if(o==null){
            throw new RPCException(ExceptionEnum.NO_SUPPORTED_INSTANCE,interfaceClass+"没有可用的实现类");
        }
        return interfaceClass.cast(o);
    }

    /**
     * 返回某个接口的所有实现类实例
     * @param interfaceCLass 接口类类型
     * @param <T> 接口类型
     * @return 实例列表
     */
    public  <T> List<T> load(Class<T> interfaceCLass){
        if(!extensionMap.containsKey(interfaceCLass.getName())){
            return Collections.emptyList();
        }
        Collection<Object> values = extensionMap.get(interfaceCLass.getName()).values();
        List<T> instances = new ArrayList<>();
        values.forEach(value->instances.add(interfaceCLass.cast(value)));
        return instances;
    }

    /**
     * 将实例化好的实例注册到map中,供后续load
     * @param interfaceClass 接口类类型
     * @param alias 实例别名
     * @param instance 实例
     */
    public void registry(Class<?> interfaceClass,String alias,Object instance){
        if(!extensionMap.containsKey(interfaceClass.getName())){
            extensionMap.put(interfaceClass.getName(),new HashMap<>());
        }
        log.info("注册bean:{interface:{},alias:{},instance:{}}",interfaceClass,alias,instance);
        extensionMap.get(interfaceClass.getName()).put(alias,instance);
    }
}
