package com.pq.rpc.transport.api.support;

import com.pq.rpc.common.domain.GlobalRecycler;
import com.pq.rpc.common.domain.Message;
import com.pq.rpc.common.domain.RPCRequest;
import com.pq.rpc.common.domain.RPCResponse;
import com.pq.rpc.config.ServiceConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 执行RPC任务的任务执行器
 * 根据ServiceConfig信息调用真正的实现类
 *
 * @author pengqi
 * create at 2019/6/27
 */
@Slf4j
public class RPCTaskRunner implements Runnable{

    private RPCRequest request;             //RPC请求对象

    private ServiceConfig serviceConfig;     //服务配置对象

    private ChannelHandlerContext ctx;      //RPC请求所在channel的ChannelHandlerContext对象,用于将RPCResponse刷出站

    public RPCTaskRunner(ChannelHandlerContext ctx,RPCRequest request,ServiceConfig serviceConfig){
        this.request = request;
        this.serviceConfig = serviceConfig;
        this.ctx = ctx;
    }

    @Override
    public void run() {
        //调用请求有两种情况:
        //1)正常的RPC请求
        //2)RPC回调请求
        if(serviceConfig.isCallback()){
            //如果该请求是从客户端传入的回调请求,则需要以代理的方式调用,因为要生成RPCRequest对象
            try{
                handle(request);
                //将request回收至对象池
                request.recycle();
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }
            return;
        }
        //从对象池中取RPCResponse对象进行复用
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
    }

    /**
     * 核心方法,通过反射调用实现,返回执行结果
     * @param request RPC请求对象
     * @return 执行结果
     * @throws Throwable 异常对象
     */
    private Object handle(RPCRequest request) throws Throwable{
        Object serviceBean = serviceConfig.getRef();    //从serviceConfig中取出服务实现的代理对象

        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Method method = serviceBeanClass.getMethod(methodName,parameterTypes);  //得到Method对象
        method.setAccessible(true);     //将method设置为可访问的

        if(serviceConfig.isCallback()){
            //当RPC请求为回调请求时,参数列表中对应的接口参数的调用要设置成代理方式,并在代理类的invoke方法中做RPC调用
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
        }
        //反射调用serviceBean的method方法并返回
        return method.invoke(serviceBean,parameters);
    }
}
