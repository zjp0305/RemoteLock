package com.remote.lock;

import com.remote.lock.connector.ConnectorFactory;
import com.remote.lock.connector.RemoteConnector;

public class ZkLockTest {
  public static void main(String[] args) throws Exception {
    String address = "zk://127.0.0.1:2181/remoteNode";
    RemoteConnector remoteConnector = ConnectorFactory.getRemoteProtoServer(address);
    RemoteReentrantLock remoteLock = new RemoteReentrantLock(remoteConnector);
    remoteLock.lock();
    System.out.println("Thread.currentThread().getId():" + Thread.currentThread().getId());
    Thread.sleep(5000);
    remoteLock.unlock();
  }
}
