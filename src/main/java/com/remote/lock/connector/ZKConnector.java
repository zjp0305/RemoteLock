package com.remote.lock.connector;

import com.remote.lock.command.CommandEnum;
import com.remote.lock.command.ICommand;
import com.remote.lock.entry.HostEntry;
import com.remote.lock.util.CommandInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class ZKConnector extends CommonConnector {

  private byte[] defaultData = new byte[] {};

  private ZooKeeper zooKeeper;

  @Override
  public void connect() throws Exception {
    StringBuffer sb = new StringBuffer();
    String sep = "";
    List<HostEntry> protocol = entry.getProtocolEntries();
    for (HostEntry e : protocol) {
      sb.append(sep);
      sb.append(e.getHost());
      sb.append(":");
      sb.append(e.getPort());
      sep = ",";
    }
    CountDownLatch countDownLatch = new CountDownLatch(1);
    this.zooKeeper =
        new ZooKeeper(sb.toString(), timeout, new ConnectWatch(entry.getPath(), countDownLatch));
    countDownLatch.await();
    this.startKeepAlive();
  }

  @Override
  public String createNode(String prefix) throws Exception {
    this.prefix = prefix;
    return this.zooKeeper.create(
        entry.getPath() + "/" + prefix,
        defaultData,
        ZooDefs.Ids.OPEN_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL);
  }

  public List<String> getChildren(String childrenNode)
      throws KeeperException, InterruptedException {
    List<String> children = this.zooKeeper.getChildren(childrenNode, false);
    return children;
  }

  @Override
  public boolean exists(String childrenNode) throws KeeperException, InterruptedException {
    Stat stat = this.zooKeeper.exists(childrenNode, new ExistsWatch());
    if (stat != null) {
      return true;
    }
    return false;
  }

  @Override
  public boolean deleteNode(String node) throws Exception {
    Stat stat = this.zooKeeper.exists(node, false);
    if (stat != null) {
      this.zooKeeper.delete(node, stat.getVersion());
    }
    return true;
  }

  class ConnectWatch implements Watcher {
    private String nodeName;

    private CountDownLatch countDownLatch;

    public ConnectWatch(String nodeName, CountDownLatch countDownLatch) {
      this.nodeName = nodeName;
      this.countDownLatch = countDownLatch;
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
      KeeperState state = watchedEvent.getState();
      switch (state) {
        case SyncConnected:
          {
            try {
              if (!exists(this.nodeName)) {
                zooKeeper.create(
                    this.nodeName, defaultData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
              }

            } catch (Exception e) {
              log.error("create node:", e);
            } finally {
              this.countDownLatch.countDown();
            }
            break;
          }
        default:
          {
            this.countDownLatch.countDown();
          }
      }
    }
  }

  class ExistsWatch implements Watcher {

    @Override
    public void process(WatchedEvent watchedEvent) {
      if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
        CommandInstance.getInstance()
            .invoke(
                new ICommand() {
                  @Override
                  public String key() {
                    return CommandEnum.DELETE.getKey();
                  }
                });
      }
    }
  }

  public void keepAlive(String childrenNode) {
    try {
      this.zooKeeper.exists("/", false);
    } catch (Exception e) {
    }
  }
}
