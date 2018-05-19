package com.remote.lock;

import com.remote.lock.connector.RemoteConnector;
import com.remote.lock.entry.GroupProtocolEntry;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class RemoteWriteReadReentrantLock implements ReadWriteLock {
  private RemoteReadReentrantLock readLock;

  private RemoteWriteReentrantLock writeLock;

  private String readPrefix = "readLock_";

  private String writePrefix = "writeLock_";

  public RemoteWriteReadReentrantLock(RemoteConnector remoteConnector) throws Exception {
    readLock = new RemoteReadReentrantLock(remoteConnector);
    writeLock = new RemoteWriteReentrantLock(remoteConnector);
  }

  @Override
  public Lock readLock() {
    return readLock;
  }

  @Override
  public Lock writeLock() {
    return writeLock;
  }

  class RemoteReadReentrantLock extends RemoteReentrantLock {
    public RemoteReadReentrantLock(RemoteConnector remoteConnector) throws Exception {
      super(remoteConnector);
      this.prefix = readPrefix;
    }

    @Override
    protected boolean holdLock() throws Exception {
      GroupProtocolEntry entry = remoteConnector.getProtocol();
      List<String> children = remoteConnector.getChildren(entry.getPath());
      children = filter(children, writePrefix);
      sort(children);
      int index = getIndex(children);
      int minIndex = index - 1;
      while (minIndex >= 0) {
        String childrenNext = children.get(minIndex);
        boolean isExists = remoteConnector.exists(entry.getPath() + "/" + childrenNext);
        if (isExists) {
          return false;
        } else {
          minIndex--;
        }
      }
      return true;
    }
  }

  class RemoteWriteReentrantLock extends RemoteReentrantLock {
    public RemoteWriteReentrantLock(RemoteConnector remoteConnector) throws Exception {
      super(remoteConnector);
      this.prefix = writePrefix;
    }

    @Override
    protected boolean holdLock() throws Exception {
      GroupProtocolEntry entry = remoteConnector.getProtocol();
      List<String> children = remoteConnector.getChildren(entry.getPath());
      children = filter(children, writePrefix, readPrefix);
      sort(children);
      int index = getIndex(children);
      int minIndex = index - 1;
      while (minIndex >= 0) {
        String childrenNext = children.get(minIndex);
        boolean isExists = remoteConnector.exists(entry.getPath() + "/" + childrenNext);
        if (isExists) {
          return false;
        } else {
          minIndex--;
        }
      }
      return true;
    }
  }
}
