package com.web.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.StartLogFactor5;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;

/**
 * Created by lihao on 2017/11/6.
 */
public class LockService {
    private static final Logger logger = LogManager.getLogger(LockService.class);
    private static final String CONNECTION_STRING = "192.168.50.102:2181";
    private static final String GROUP_PATH = "/testdislocks";
    private static final String SUB_PATH = "/testdislocks/sub";
    private static final int SESSION_TIMEOUT = 10000;
    AbstractZookeeper az = new AbstractZookeeper();

    public void doService(DoTemplate doTemplate){
        try {
            ZooKeeper zk = az.connect(CONNECTION_STRING,SESSION_TIMEOUT);
            DistributedLock dc = new DistributedLock(zk);
            LockWatcher lw = new LockWatcher(dc,doTemplate);
            dc.setWatcher(lw);
            dc.createPath(GROUP_PATH,"该节点由线程"+Thread.currentThread().getName() + "创建");
            boolean rs = dc.getLock();
            if(rs){
                lw.dosomething();
                dc.unlock();
            }
        }catch (Exception e){

        }
    }
}
