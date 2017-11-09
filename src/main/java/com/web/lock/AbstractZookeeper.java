package com.web.lock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lihao on 2017/11/6.
 */
public class AbstractZookeeper implements Watcher {

    protected ZooKeeper zooKeeper;
    protected CountDownLatch countDownLatch = new CountDownLatch(1);

    public void process(WatchedEvent event) {
        if(event.getState() == Event.KeeperState.SyncConnected){
            countDownLatch.countDown();
        }
    }

    public ZooKeeper connect(String hosts,int SESSION_TIMEOUT) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(hosts,SESSION_TIMEOUT,this);
        countDownLatch.await();
        System.out.println("AbstractZookeeper connect!"+Thread.currentThread().getName());
        return zooKeeper;
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }
}
