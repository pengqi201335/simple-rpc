package com.pq.rpc.executor.threadPool;

import com.pq.rpc.executor.api.support.AbstractTaskExecutor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于线程池技术的任务执行器
 * 使用Executors框架
 * execute和submit的区别,前者没有返回值,后者返回一个future对象
 *
 * @author pengqi
 * create at 2019/6/27
 */
public class ThreadPoolTaskExecutor extends AbstractTaskExecutor {

    /**
     * 线程池
     */
    private ExecutorService executorService;

    @Override
    public void close() {
        executorService.shutdown();
    }

    @Override
    public void submit(Runnable task) {
        executorService.submit(task);
    }

    @Override
    public void init(int threads) {
        executorService = new ThreadPoolExecutor(
                threads,                                    // 核心线程数
                threads,                                    // 最大线程数
                0,                              // 空闲线程存活时间
                TimeUnit.MILLISECONDS,                      // 时间单位
                new LinkedBlockingDeque<>(),                // 阻塞队列
                new ThreadFactory() {                       // 线程工厂
                    //线程编号
                    private AtomicInteger threadCount = new AtomicInteger(0);
                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r,"thread-"+threadCount.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()   // 拒绝策略(由调用线程处理该任务)
        );
    }
}
