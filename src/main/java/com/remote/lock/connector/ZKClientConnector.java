package com.remote.lock.connector;

import com.remote.lock.command.CommandEnum;
import com.remote.lock.command.ICommand;
import com.remote.lock.entry.HostEntry;
import com.remote.lock.util.CommandInstance;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.util.List;

public class ZKClientConnector extends CommonConnector implements RemoteConnector {

  private ZkClient zkClient;

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
    this.zkClient = new ZkClient(sb.toString(), timeout);
    if (!exists(entry.getPath(), false)) {
      this.zkClient.create(entry.getPath(), "", CreateMode.PERSISTENT);
    }
    this.startKeepAlive();
  }

  @Override
  public String createNode(String prefix) throws Exception {
    this.prefix = prefix;
    return this.zkClient.create(
        entry.getPath() + "/" + prefix,
        "",
        ZooDefs.Ids.OPEN_ACL_UNSAFE,
        CreateMode.EPHEMERAL_SEQUENTIAL);
  }

  @Override
  public List<String> getChildren(String childrenNode)
      throws KeeperException, InterruptedException {
    return this.zkClient.getChildren(childrenNode);
  }

  @Override
  public boolean exists(String childrenNode) throws KeeperException, InterruptedException {
    return exists(childrenNode, true);
  }

  @Override
  public boolean deleteNode(String node) throws Exception {
    boolean isExists = this.zkClient.exists(node);
    if (isExists) {
      this.zkClient.delete(node);
    }
    return true;
  }

  public void keepAlive(String childrenNode) {
    try {
      exists("/", false);
    } catch (Exception e) {
    }
  }

  private void watch(String childrenNode) {
    this.zkClient.subscribeChildChanges(
        childrenNode,
        new IZkChildListener() {
          @Override
          public void handleChildChange(String parentPath, List<String> currentChilds)
              throws Exception {
            CommandInstance.getInstance()
                .invoke(
                    new ICommand() {
                      @Override
                      public String key() {
                        return CommandEnum.DELETE.getKey();
                      }
                    });
          }
        });
  }

  private boolean exists(String childrenNode, boolean watch)
      throws KeeperException, InterruptedException {
    if (watch) {
      watch(childrenNode);
    }
    return this.zkClient.exists(childrenNode);
  }
}
