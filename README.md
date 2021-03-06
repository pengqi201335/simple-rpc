# simple-rpc project
simple-rpc是一个基于netty+springboot+zookeeper的分布式服务框架，具有服务注册与发现、远程服务调用和集群负载均衡等基本功能。架构上参考了[dubbo](https://github.com/apache/dubbo)的分层设计，各功能模块放在不同的包中，通过API相互关联实现解耦。
## 功能列表
* 基于[Netty](https://github.com/netty/netty)实现客户端与服务端之间的通信，包括空闲检测、心跳保持、解决粘包半包等问题
* 基于[zookeeper](https://zookeeper.apache.org/)实现服务的注册与发现，利用zookeeper的监听机制实现服务的订阅/推送
* 实现了一致性哈希/轮询/随机/加权随机和最小活跃度等负载均衡算法
* 实现了failover/failfast/failsafe三种集群容错机制
* 基于springboot的自动配置功能实现bean的加载，并且自定义了一个springboot-starter插件供用户使用
* 实现简易扩展点(类似Java的SPI机制)
* 实现了jdk/hessian/protostuff/json等多种序列化算法
## 分层设计
分层设计是为了解耦，下层向上层暴露接口，上层只需要调用下层的接口而无需关心实现，具体实现类由用户通过配置文件来配置，避免硬编码。
### Config层
config层为配置层，定义了各功能模块的配置类，主要包括协议配置、序列化方式配置、注册中心配置、集群负载均衡和容错机制配置等。每个配置类中拥有若干对应的功能实例，根据用户的配置文件，通过枚举单例的方式注入依赖。

整个应用中的依赖注入是在simple-rpc-spring-boot-starter包中完成的，该包引入了simple-rpc-core，通过autoConfiguration的方式，在RPCAutoConfiguration类中完成了所有配置类的bean加载和依赖注入。以引用服务配置类和暴露服务配置类为例，说明依赖注入的实现细节。

#### ReferenceConfig
ReferenceConfig是服务引用配置类，定义了引用服务的类型及其引用方式(异步、同步、回调等)，其内部持有远程服务的本地代理和服务的抽象调用者，二者在ReferenceConfig被配置时初始化。而ReferenceConfig则是在spring容器初始化完成之后，对每个Bean调用后置处理器时，在后置处理器中被初始化的。

即，先通过springboot的自动配置功能将用户配置文件中的实例和用户自定义bean配置完成，并注册到spring容器中，然后对每个bean调用后置处理器，扫描其所有字段，看是否有`@RPCReference`注解，一旦发现有该注解，就根据该注解的属性创建一个ReferenceConfig实例，调用代理工厂生成对应的远程服务本地代理，注入到该字段中。如下代码所示：
```java
//扫描bean的所有字段
Field[] fields = beanClass.getDeclaredFields();
for(Field field:fields){
    //获取字段类型(对引用服务来说就是接口类型)
    Class<?> interfaceClass = field.getType();
    //获取字段的@RPCReference注解
    RPCReference reference = field.getAnnotation(RPCReference.class);
    if(reference!=null){
        //字段有@RPCReference注解,使用注解配置ReferenceConfig对象
        ReferenceConfig referenceConfig = ReferenceConfig.createReferenceConfig(
                interfaceClass.getName(),
                interfaceClass,
                reference.async(),
                //省略
                ExtensionLoader.getINSTANCE().load(Filter.class)
        );
        try{
            field.set(bean,referenceConfig.get());  //创建一个远程服务的代理对象,将其注入到字段中
            }catch (IllegalAccessException e){
                e.printStackTrace();
            }
    }
}
```
#### ServiceConfig
ServiceConfig是服务暴露配置类，定义了服务类型及其调用方式，内部持有本地服务的代理对象和服务暴露后的抽象调用者以及一个export()方法，在ServiceConfig被初始化后立即调用该方法暴露服务(注册到zookeeper注册中心)。

ServiceConfig的配置同样是在bean的后置处理器中完成，扫描所有的bean，判断其是否有`@RPCService`注解，有的话就根据注解属性创建一个ServiceConfig实例，并将该bean注入为该服务的本地代理。如下代码所示：
```java
Class<?> beanClass = bean.getClass();
RPCService service = beanClass.getAnnotation(RPCService.class);
Class<?> interfaceClass = service.interfaceClass();
if(interfaceClass == void.class){
    //省略
}
ServiceConfig serviceConfig = ServiceConfig.builder().
        interfaceName(interfaceClass.getName()).
        //省略
        ref(bean).
        build();
initConfig(serviceConfig);
serviceConfig.export();     //暴露此服务
```
其它的配置类则是根据用户配置的application.properties文件在RPCAutoConfiguration类中自动配置，以如下代码形式进行依赖注入：
```java
applicationConfig.setRPCProxyFactoryInstance(extensionLoader.load(RPCProxyFactory.class, ProxyFactoryType.class,applicationConfig.getProxyFactoryName()));
```
`extensionLoader`是自定义的一个扩展类加载器，用于加载配置实例，加载方式按应用内依赖和应用外依赖分为两种，如下所示，应用内的依赖通过枚举单例的方式注入，而应用外的依赖则根据配置文件使用反射的方式注入：
```java
public <T> T load(Class<T> interfaceClass,Class enumType,String type){
    ExtensionBaseType<T> extensionBaseType = ExtensionBaseType.valueOf(enumType,type.toUpperCase());
    //针对应用内的依赖
    if(extensionBaseType!=null){
        return extensionBaseType.getInstance();     //返回枚举单例中对应的实现类实例
    }
    //针对应用外的依赖
    //依赖注入之前,已经读取完配置文件并将实现类实例注册在map中,此时只需要从map中取实例
    Object o = extensionMap.get(interfaceClass.getName()).get(type);
    return interfaceClass.cast(o);
}
```
扫描配置文件并创建扩展点实例的方式：
```java
public void loadResources(){
    URL parent = this.getClass().getClassLoader().getResource("/rpc");
    if(parent!=null){
        File dir = new File(parent.getFile());
        File[] files = dir.listFiles();
        for(File file:files){
            handleFile(file);
        }
    }
}
```
```java
private void handleFile(File file){
        String interfaceName = file.getName();      //配置文件名为接口的全限定名
        try{
            //...
            while ((line=reader.readLine())!=null){
                //...
                //实例化实现类
                try{
                    Class<?> impl = Class.forName(kv[1]);
                    //反射创建实例
                    Object o = impl.getDeclaredConstructor().newInstance();
                    //注册到map中
                    registry(interfaceClass,kv[0],o);
                }//...
            }
        }//...
    }
```
即扫描/rpc目录下的文件，根据文件中配置的类名创建扩展点实例并注册到map中，做依赖注入时可以直接从map中取实例注入。
### Proxy层
Proxy层为代理层，分别向ReferenceConfig和ServiceConfig提供`createProxy()`方法和`getInvoker()`方法，前者为引用远程服务的字段生成一个本地代理，后者在服务端暴露服务后返回一个抽象调用者Invoker，该invoker调用invoke()方法时实际上是在调用服务实现类的方法。

如下代码所示为jdk动态代理的实现方式：
```java
public class JDKProxyFactory extends AbstractProxyFactory {
    @Override
    @SuppressWarnings("unchecked")
    protected <T> T doCreateProxy(Invoker<T> invoker, Class<T> interfaceClass) {
        //生成代理对象
        return (T)Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class[]{interfaceClass},
                (proxy, method, args) ->
                    JDKProxyFactory.this.invokeProxyMethod(invoker,method,args)
        );
    }
}
```
可以看到，代理对象实际上是调用了invokeProxyMethod()方法，而该方法是RPC调用的真正入口，方法核心代码：
```java
request.setRequestID(UUID.randomUUID().toString());
request.setInterfaceName(interfaceName);
request.setMethodName(methodName);
request.setParameters(parameters);
RPCInvokeParam rpcInvokeParam = RPCInvokeParam.builder()
        .rpcRequest(request)
        .referenceConfig(ReferenceConfig.getReferenceConfigByInterfaceName(interfaceName))
        .build();
RPCResponse response = invoker.invoke(rpcInvokeParam);
```
该方法的主要内容就是创建RPC调用参数对象，封装RPC请求，并调用抽象调用者invoker的invoke()方法(此invoker是在配置ReferenceConfig实例时创建的ClusterInvoker(集群层面的invoker))，得到调用结果。
### Cluster层
cluster层为集群层，主要做两件事：负载均衡和集群容错。负载均衡的作用是在服务提供者列表中选择一个可用的服务进行RPC调用，集群容错是在RPC调用异常后的处理机制。
#### 负载均衡
* 一致性哈希
* 最小活跃度算法
* 轮询
* 随机
* 加权随机
#### 集群容错
* failover(失败后快速切换到下一个可用服务)
* failfast(快速失败，即失败后直接抛出异常，不再重试，适用于幂等操作)
* failsafe(安全失败，即失败后不抛异常，直接返回，适用于写日志等操作)

cluster层还有一个核心类ClusterInvoker，为集群层面的抽象调用者(引用同一服务的consumer共享一个ClusterInvoker)，ClusterInvoker维护了某个服务的服务列表，代理对象调用其invoke()方法时，先在该服务列表中做负载均衡选择一个可用服务，该可用服务的表现形式为Protocol层面的抽象调用者，实际上是调用protocolInvoker的invoke()方法。

ClusterInvoker又是怎么维护某个服务的服务列表呢？如下代码所示：
```java
private void init(){
    globalConfig.getRegistryConfig().getServiceRegistryInstance()
            .discover(interfaceName,
                      this::removeNotExisted,
                      this::addOrUpdate);
}
```
在创建ClusterInvoker时，调用init()方法做服务发现，去注册中心发现相应的服务并`注册监听事件`，利用zookeeper的Watcher机制，在服务上线、下线或更新时通过回调函数更新服务列表从而实现实时的服务发现。

### Registry层
registry层就是注册中心层，该层向外部提供的API如下所示：
```java
public interface ServiceRegistry {
    void init();
    void discover(String interfaceName,ServiceOfflineCallback serviceOfflineCallback,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback);
    void register(String serviceAddress,String interfaceName,Class<?> interfaceClass);
    void close();
}
```
主要的两个方法就是discover()和register()，分别用来发现服务和注册服务，目前只实现了zookeeper的注册中心，来看具体实现：
```java
public void discover(String interfaceName, ServiceOfflineCallback serviceOfflineCallback, ServiceAddOrUpdateCallback serviceAddOrUpdateCallback) {
    watchInterface(interfaceName,serviceOfflineCallback,serviceAddOrUpdateCallback);    //发现服务并注册监听
}

private void watchInterface(String interfaceName,ServiceOfflineCallback serviceOfflineCallback,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback){
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
    }
}
    
private ServiceURL watchService(String interfaceName,String address,ServiceAddOrUpdateCallback serviceAddOrUpdateCallback){
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
    }
}
```
服务发现的主要逻辑就是根据服务接口名从zookeeper中获取节点数据并注册监听事件，重写监听事件处理方法process()，当监听事件发生时，再一次调用方法获取zookeeper节点数据，然后将节点数据作为参数调用回调函数，在回调函数呢中更新服务列表。
```java
public void register(String serviceAddress, String interfaceName, Class<?> interfaceClass) {
    String path = generatePath(interfaceName);
    //先创建路径
    zkSupport.createPathIfAbsent(path, CreateMode.PERSISTENT);
    //后创建节点
    zkSupport.createNodeIfAbsent(serviceAddress,path);
}
```
服务注册的逻辑更简单，先根据服务接口名在zookeeper中创建路径，再根据对应的服务器IP地址和参数表创建Znode节点，即完成注册。

### Protocol层
protocol层是与底层网络通信直接交互的层，负责维护netty客户端和服务端实例以及protocol层面的Invoker。

对于客户端来说，服务发现时返回的是ServiceURL对象，这是服务注册到zookeeper中的形式，clusterInvoker中要根据该serviceURL调用protocol实例的refer()方法来获得该服务在protocol层的抽象调用者protocolInvoker，如下代码所示：
```java
public <T> Invoker<T> refer(ServiceURL serviceURL, ReferenceConfig<T> referenceConfig) {
    //创建一个面向James协议的调用者,负责提供getProcessor()方法与transport层交互
    JamesInvoker<T> invoker = new JamesInvoker<>();
    invoker.setInterfaceName(referenceConfig.getInterfaceName());
    invoker.setInterfaceClass(referenceConfig.getInterfaceClass());
    invoker.setGlobalConfig(getGlobalConfig());
    invoker.setClient(initClient(serviceURL));
    List<Filter> filters = referenceConfig.getFilters();
    if(filters.size()==0){
        //没有配置过滤器,直接返回invoker
        return invoker;
    }
    //配置了过滤器,则要构建过滤链
    return invoker.buildFilterChain(filters);
}
```
从以上代码可以看出，每个protocolInvoker都对应一个netty的客户端实例(目标服务器相同的invoker共享一个客户端实例)，做RPC调用时会通过该client实例提交RPC请求，并且在该invoker上又做了一层封装，主要用于过滤器链，过滤器用于在真正发起远程调用前做一些前置处理或远程调用结束返回时做一些后置处理，如统计某些服务被调用的次数用于最小活跃度负载均衡。

而对于服务端来说，需要调用protocol实例的export()方法将服务以ServiceURL的形式注册到zookeeper中，如下代码所示：
```java
public <T> Exporter<T> export(Invoker<T> localInvoker, ServiceConfig<T> serviceConfig) {
    JamesExporter<T> exporter = new JamesExporter<>();
    exporter.setInvoker(localInvoker);
    exporter.setServiceConfig(serviceConfig);
    //将暴露的服务放入缓存中
    putExporter(localInvoker.getInterfaceClass(),exporter);
    //暴露服务之前先开启服务端连接,防止出现服务端连接还未开启客户端就拿到服务地址并请求建立连接的情况
    openServer();
    try{
        //将服务暴露到注册中心
        serviceConfig.getRegistryConfig().getServiceRegistryInstance().register(
                //主机IP+端口号
                "192.168.1.116"+":"+getGlobalConfig().getPort(),
                localInvoker.getInterfaceName(),
                localInvoker.getInterfaceClass());
    }
    return exporter;
}
```
主要就是将服务以ServiceURL的形式注册到zookeeper，并缓存在服务端的map中，供后续服务端处理RPC请求时使用。需要注意的一点是，在暴露服务之前要先开启netty服务端连接，防止客户端在zookeeper中拿到服务器地址并建立连接时，服务端连接还没有打开的情况出现。

protocol层面的invoke逻辑也比较简单，在AbstractInvoker中实现：
```java
public RPCResponse invoke(InvokeParam invokeParam) throws RPCException {
    Function<RPCRequest,Future<RPCResponse>> logic = getProcessor();
    if(logic==null){
        throw new RPCException(ExceptionEnum.GET_PROCESSOR_METHOD_MUST_BE_OVERRIDE,"GET_PROCESSOR_METHOD_MUST_BE_OVERRIDE");
    }
    //根据用户的配置选择具体的调用方法并进行远程调用
    return InvocationType.get(invokeParam).invoke(invokeParam,logic);
}
```
用到了函数式编程，调用具体协议invoker的getProcessor()方法创建一个函数对象，该函数逻辑就是向netty客户端提交RPC请求，然后选择具体的调用方式(同步调用、异步调用等)apply该函数，获取调用结果。面向James协议的invoker的getProcessor()方法如下所示：
```java
protected Function<RPCRequest, Future<RPCResponse>> getProcessor() {
    //返回一个和传输层交互的函数
    return request -> getClient().submit(request);
}
```
可以看出是调用对应netty客户端的submit()方法提交请求。

### Transport层
tansport层负责创建并维护客户端与服务端之间的网络通信，基于netty框架实现，可以方便地进行异步通信。
#### 客户端
客户端API如下所示：
```Java
public interface Client {
    Future<RPCResponse> submit(RPCRequest request);
    void close();
    void handlerException(Throwable throwable);
    void handleRPCResponse(RPCResponse response);
    void handleCallbackRequest(RPCRequest request, ChannelHandlerContext ctx);
    void updateServiceConfig(ServiceURL serviceURL);
    ServiceURL getServiceURL();
    boolean isAvailable();
}
```
* submit()方法用于提交RPC请求，返回一个Future对象用于实现异步调用
* handleXXX方法用于处理对应的事件（异常、回调、响应）
* updateServiceConfig()方法用于更新配置信息(serviceURL)
前面已经提到，client实例是在protocol层被创建并初始化的，在AbstractRemoteProtocol中维护了一个哈希表，k-v分别表示服务器地址和客户端实例，protocol对象根据RPC请求对应的serviceURL中的服务器IP地址来判断是否要新建一个客户端，若哈希表中缓存了对应服务器地址的客户端则复用该客户端，防止重复创建，否则就调用基于netty的client类的connect()方法连接服务器。
连接成功之后，调用submit()方法提交请求，返回一个Future对象，要将这个Future对象存入一个线程共享的全局map中，该map的k-v分别表示{请求ID,Future对象}，这个map主要用于回调，因为如果调用方式是回调的话，请求线程和eventLoop线程很可能不是同一个，如下所示：
```Java
public class RPCThreadSharedContext {
    private static final Map<String,CompletableFuture<RPCResponse>> RESPONSES = new ConcurrentHashMap<>();
    private static final Map<String, ServiceConfig> HANDLER_MAP = new ConcurrentHashMap<>();
}
```
同步、异步和oneWay调用的响应处理比较简单，来看看回调方式的响应处理方法：
```Java
public void handleCallbackRequest(RPCRequest request, ChannelHandlerContext ctx) {
    ServiceConfig serviceConfig = RPCThreadSharedContext.getAndRemoveHandler(
            CallbackInvocation.generateCallbackHandlerKey(request,
                    ReferenceConfig.getReferenceConfigByInterfaceName(request.getInterfaceName()))
    );
    //将回调任务提交给线程池处理
    getGlobalConfig().getClientExecutor().submit(new RPCTaskRunner(ctx,request,serviceConfig));
}
```
回调响应实际上是一个RPC请求，且该RPC请求与当初客户端发送的RPC请求共享同一个requestID，根据此ID去HANDLER_MAP中拿到对应的serviceConfig，再提交到线程池中处理，任务处理逻辑如下：
```Java
RPCResponse response = GlobalRecycler.reuse(RPCResponse.class);
response.setRequestID(request.getRequestID());
try{
    Object result = handle(request);    //通过反射调用返回的执行结果
    response.setResult(result);
}catch (Throwable throwable){
    throwable.printStackTrace();
    response.setErrorCause(throwable);
}
log.info("本地调用执行完成,执行结果:{}",response);
//如果调用方式为callback,且该请求为服务端返回的回调请求,则不需要响应
if(!serviceConfig.isCallbackInterface()){
    ctx.writeAndFlush(Message.buildResponse(response));
}
```
```Java
Object serviceBean = serviceConfig.getRef();    //从serviceConfig中取出服务实现的代理对象
Class<?> serviceBeanClass = serviceBean.getClass();
String methodName = request.getMethodName();
Class<?>[] parameterTypes = request.getParameterTypes();
Object[] parameters = request.getParameters();
Method method = serviceBeanClass.getMethod(methodName,parameterTypes);  //得到Method对象
return method.invoke(serviceBean,parameters);
```
原理是通过反射调用返回的执行结果，其中serviceConfig是调用时生成的，封装了具体回调对象。

#### 服务端
服务端API如下所示：
```Java
public interface Server {

    /**
     * 运行服务端
     */
    void run();

    /**
     * 关闭服务端连接
     */
    void close();

    /**
     * RPC请求处理函数
     * @param request RPC请求
     * @param ctx 用于标记handler,以便任务在线程池中处理完之后能从正确的channel中被刷出
     */
    void handlerRPCRequest(RPCRequest request, ChannelHandlerContext ctx);
}
```
服务端的接口方法比较简单，其开启时机是在暴露服务之前，同样也是protocol来管理，handlerRPCRequest()方法则是在收到请求消息后被调用，同样的，也是交给任务线程池处理，比较特殊的是回调请求的处理，如下所示：
```Java
Class<?> interfaceClass = parameterTypes[serviceConfig.getCallbackParamIndex()];
//创建回调接口的代理对象
parameters[serviceConfig.getCallbackParamIndex()] = Proxy.newProxyInstance(
        interfaceClass.getClassLoader(),
        new Class<?>[]{interfaceClass},
        (proxy, method1, args) -> {
            if(method1.getName().equals(serviceConfig.getCallbackMethod())){
                //从对象池中复用RPCRequest对象
                RPCRequest rpcRequest = GlobalRecycler.reuse(RPCRequest.class);
                //与客户端的RPC请求编号相同,以便在全局map中找到对应的serviceConfig(callback实例)
                rpcRequest.setRequestID(request.getRequestID());
                rpcRequest.setInterfaceName(method1.getDeclaringClass().getName());
                rpcRequest.setMethodName(method1.getName());
                rpcRequest.setParameterTypes(method1.getParameterTypes());
                rpcRequest.setParameters(args);
                ctx.writeAndFlush(rpcRequest);  //发起RPC调用
                return null;
            }else{
                return method1.invoke(proxy,args);
            }
        }
);
```
服务端会创建一个代理对象来处理回调，在代理对象中构造RPC请求，此时服务端的身份变成了RPC客户端，向发起回调请求的客户端写RPC请求。
#### 心跳检测
客户端和服务端之间实现了心跳检测机制，利用netty自带的IdleStateHandler实现，在指定的时间内没有读事件或写事件将会触发userEventTriggered()方法，该方法内会将一个重连计数器原子自增1，并判断该计数器是否达到最大重连次数，若达到了，直接关闭连接，没有的话就继续等待。重连计数器由一个AtomicInteger类实现，防止并发修改导致错误。客户端或服务端收到消息的第一件事就是将该计数器置为0.
```Java
private AtomicInteger timeoutCount = new AtomicInteger(0);  //原子类,重试次数
```
```Java
/** 客户端 */
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    //先判断事件是否空闲事件
    if(evt instanceof IdleStateEvent){
        if(timeoutCount.getAndIncrement()>= JamesConstant.HEART_BEAT_TIMEOUT_MAX_TIMES){
            //超过心跳重试次数仍未收到消息,可以判断服务器宕机或应用不可用
            //关闭连接并重连
            client.handlerException(new RPCException(ExceptionEnum.HEART_BEAT_TIMES_EXCEED,"HEART_BEAT_TIMES_EXCEED"));
        }else{
            //还未超过心跳重连次数
            //发送心跳信号
            log.info("指定时间内为收到服务端消息,发送心跳信号至{}",client.getServiceURL().getServiceAddress());
            ctx.channel().writeAndFlush(Message.PING_MSG);
        }
    }else{
        super.userEventTriggered(ctx, evt);
    }
}
```
```Java
/** 服务端 */
public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if(evt instanceof IdleStateEvent){
        //空闲事件被触发
        if(timeoutCount.getAndIncrement()> JamesConstant.HEART_BEAT_TIMEOUT_MAX_TIMES){
            log.info("超过丢失心跳最大次数,关闭服务端连接...");
            ctx.close();
        }else{
            log.error("规定时间内服务器未接收到客户端消息或心跳信息...");
        }
    }else{
        super.userEventTriggered(ctx, evt);
    }
}
```
服务端并不主动发送心跳信号，而是在收到客户端的PING信号后响应一个PONG信号。