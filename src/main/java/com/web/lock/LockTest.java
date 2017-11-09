package com.web.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.CountDownLatch;

/**
 * Created by lihao on 2017/11/6.
 */
public class LockTest {
    private static final int THREAD_NUM = 10;
    public static CountDownLatch threadSemaphore = new CountDownLatch(THREAD_NUM);

    public static void main(String[] args) {
        for (int i = 0;i < THREAD_NUM;i++){
            final int threadId = i;
            new Thread(){
                public void run(){
                    try{
                        new LockService().doService(new DoTemplate() {
                            public void dodo() {
                                System.out.println("修改了一个文件。。。"+threadId);
                            }
                        });
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        try {
            threadSemaphore.await();
            System.out.println("所有线程运行结束");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
