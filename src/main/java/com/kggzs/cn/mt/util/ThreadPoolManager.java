package com.kggzs.cn.mt.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程池管理器，提供统一的线程池替代所有 {@code new Thread(...)} 直接创建方式
 */
public class ThreadPoolManager {

    private static volatile ExecutorService instance;
    private static final AtomicInteger threadCounter = new AtomicInteger(1);

    private ThreadPoolManager() {
    }

    /**
     * 获取线程池单例（使用缓存线程池，自动回收空闲线程）
     */
    public static ExecutorService getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = Executors.newCachedThreadPool(r -> {
                        Thread t = new Thread(r);
                        t.setDaemon(true);
                        t.setName("MTKang-Worker-" + threadCounter.getAndIncrement());
                        return t;
                    });
                }
            }
        }
        return instance;
    }

    /**
     * 在后台线程执行任务
     */
    public static void execute(Runnable task) {
        getInstance().execute(task);
    }
}