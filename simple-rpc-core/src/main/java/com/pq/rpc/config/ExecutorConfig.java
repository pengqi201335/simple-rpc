package com.pq.rpc.config;

import com.pq.rpc.executor.api.TaskExecutor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * executor配置类，主要配置以下属性
 * 1)线程池线程数量
 * 2)executor类型，有两种:disruptor高性能队列、线程池(Executors框架)
 * 3)TaskExecutor实例
 *
 * @author pengqi
 * create at 2019/6/21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutorConfig {

    /**
     * 默认线程数为处理器核心数量
     */
    public static final Integer DEFAULT_THREADS = Runtime.getRuntime().availableProcessors();

    private Integer threads;

    private String type;

    private TaskExecutor taskExecutorInstance;

    /**
     * 返回配置的线程数
     * @return threads
     */
    public int getThreads(){
        if(threads!=null){
            return threads;
        }
        return DEFAULT_THREADS;
    }

    /**
     * 关闭线程池
     */
    public void close(){
        if(taskExecutorInstance!=null){
            taskExecutorInstance.close();
        }
    }
}
