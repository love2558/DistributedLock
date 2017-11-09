package com.web.lock;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

/**
 * Created by lihao on 2017/11/6.
 */
public class DistributedLock {

    private ZooKeeper zk = null;
    private String selfPath;
    private String waitPath;
    private String LOG_PREFIX_OF_THREAD = Thread.currentThread().getName();
    private static final String GROUP_PATH = "/testdislocks";
    private static final String SUB_PATH = "/testdislocks/sub";

    private static  final Logger logger = LogManager.getLogger(DistributedLock.class);

    private Watcher watcher;
    public DistributedLock(ZooKeeper zooKeeper){
        this.zk = zooKeeper;
    }

    public String getSelfPath() {
        return selfPath;
    }

    public void setSelfPath(String selfPath) {
        this.selfPath = selfPath;
    }

    public String getWaitPath() {
        return waitPath;
    }

    public void setWaitPath(String waitPath) {
        this.waitPath = waitPath;
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }

    public boolean createPath(String path,String data) throws KeeperException, InterruptedException {
        if(zk.exists(path,false) == null){
            System.out.println(LOG_PREFIX_OF_THREAD + "节点创建成功，Path:"
                    + this.zk.create(path,data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT)
                    + ",content: " + data);
        }
        return true;
    }

    public boolean getLock() throws Exception{
        selfPath = zk.create(SUB_PATH,null,ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(LOG_PREFIX_OF_THREAD + "创建分布式锁节点路径："+selfPath);
        if(checkMinPath()){
            return true;
        }
        return false;
    }

    public void unlock(){
        try {
            if(zk.exists(this.selfPath,false) == null){
                System.out.println(LOG_PREFIX_OF_THREAD + "该节点已不存在了。。。");
                return;
            }
            zk.delete(this.selfPath,-1);
            System.out.println(LOG_PREFIX_OF_THREAD + "删除节点：" + selfPath);
            zk.close();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkMinPath() throws Exception {
        List<String> subNodes = zk.getChildren(GROUP_PATH,false);
        Collections.sort(subNodes);
        String s = selfPath.substring(GROUP_PATH.length()+1);
        int index = subNodes.indexOf(selfPath.substring(GROUP_PATH.length()+1));
        switch(index){
            case -1 : {
                System.out.println(LOG_PREFIX_OF_THREAD + "本节点已不存在了。。。。"+selfPath);
                return false;
            }
            case 0 : {
                System.out.println(LOG_PREFIX_OF_THREAD + "子节点中，我排在第一个了！"+selfPath );
                return true;
            }
            default : {
                this.waitPath = GROUP_PATH + "/" + subNodes.get(index - 1);
                System.out.println(LOG_PREFIX_OF_THREAD + "子节点中，排在我前面的是：" + this.waitPath +" "+Thread.currentThread().getName());
                try {
                    zk.getData(waitPath,this.watcher,new Stat());//监控前面节点的状态
                    return false;
                }catch (Exception e){
                    if(zk.exists(waitPath,false) == null){
                        System.out.println(LOG_PREFIX_OF_THREAD + "子节点中，排在我前面的"+waitPath+"已失踪");
                        return checkMinPath();
                    }else{
                        throw e;
                    }
                }

            }
        }
    }
}
