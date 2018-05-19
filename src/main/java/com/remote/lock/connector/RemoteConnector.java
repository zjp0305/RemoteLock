package com.remote.lock.connector;

import com.remote.lock.entry.GroupProtocolEntry;
import org.apache.zookeeper.KeeperException;

import java.util.List;

public interface RemoteConnector {
  void setProtocol(GroupProtocolEntry entry);

  GroupProtocolEntry getProtocol();

  void connect() throws Exception;

  String createNode(String prefix) throws Exception;

  List<String> getChildren(String childrenNode) throws KeeperException, InterruptedException;

  boolean deleteNode(String node) throws Exception;

  boolean exists(String childrenNode) throws Exception;
}
