package com.pq.rpc.executor.api;


/**
 * 任务执行器接口,主要有以下3个方法:
 * 1)关闭线程池
 * 2)向线程池提交任务
 * 3)初始化线程池
 *
 * @author pengqi
 * create at 2019/6/21
 */
public interface TaskExecutor {

    void close();

    void submit(Runnable task);

    void init(int threads);
}
