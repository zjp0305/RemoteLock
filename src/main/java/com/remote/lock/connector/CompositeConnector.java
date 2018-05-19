package com.remote.lock.connector;

import com.remote.lock.entry.GroupProtocolEntry;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;

public class CompositeConnector extends CommonConnector implements RemoteConnector {

  private List<RemoteConnector> remoteProtoServer = new ArrayList<>();

  private RemoteConnector currRemoteConnector = null;

  public GroupProtocolEntry getProtocol() {
    return curr().getProtocol();
  };

  @Override
  public void setProtocol(GroupProtocolEntry entry) {
    curr().setProtocol(entry);
  }

  @Override
  public String createNode(String prefix) throws Exception {
    return curr().createNode(prefix);
  }

  @Override
  public List<String> getChildren(String childrenNode)
      throws KeeperException, InterruptedException {
    return curr().getChildren(childrenNode);
  }

  @Override
  public boolean deleteNode(String node) throws Exception {
    return curr().deleteNode(node);
  }

  @Override
  public boolean exists(String childrenNode) throws Exception {
    return currRemoteConnector.exists(childrenNode);
  }

  @Override
  public void connect() throws Exception {
    currRemoteConnector.connect();
  }

  public void addRemoteServer(RemoteConnector remoteServer) {
    currRemoteConnector = remoteServer;
    remoteProtoServer.add(remoteServer);
  }

  private RemoteConnector curr() {
    RemoteConnector remoteConnector = null;
    try {
      if (exists("/")) {
        return currRemoteConnector;
      }
    } catch (Exception e) {
      remoteConnector = null;
    }

    for (RemoteConnector c : remoteProtoServer) {
      try {
        if (c.exists("/")) {
          remoteConnector = c;
          currRemoteConnector = c;
          return remoteConnector;
        }
      } catch (Exception e) {
        continue;
      }
    }

    return remoteConnector;
  }

  public void keepAlive(String childrenNode) {
    try {
      this.exists(childrenNode);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
