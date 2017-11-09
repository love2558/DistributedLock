package com.web.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Created by lihao on 2017/11/6.
 */
public class LockWatcher implements Watcher {
    private static final Logger log = LogManager.getLogger(LockWatcher.class);
    private String waitPath;
    private DistributedLock distributedLock;
    private DoTemplate doTemplate;

    public LockWatcher(DistributedLock distributedLock,DoTemplate doTemplate){
        this.distributedLock = distributedLock;
        this.doTemplate = doTemplate;
    }

    public String getWaitPath() {
        return waitPath;
    }

    public void setWaitPath(String waitPath) {
        this.waitPath = waitPath;
    }

    public void process(WatchedEvent event) {
        if(event.getType() == Event.EventType.NodeDeleted && event.getPath().equals(distributedLock.getWaitPath())){
            System.out.println(Thread.currentThread().getName()+"收到情报，排在前面的已挂，轮到我了");
            try {
                if(distributedLock.checkMinPath()){
                    dosomething();
                    distributedLock.unlock();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void dosomething(){
        System.out.println(Thread.currentThread().getName()+"获取分布式锁，开始干活！");
        doTemplate.dodo();
        LockTest.threadSemaphore.countDown();
    }
}
