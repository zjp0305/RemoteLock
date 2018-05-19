package com.remote.lock;

import com.remote.lock.connector.ConnectorFactory;
import com.remote.lock.connector.RemoteConnector;

public class ZkcWriteReadLock {
  public static void main(String[] args) throws Exception {
    String address = "zkc://127.0.0.1:2181/remoteNode";
    RemoteConnector remoteConnector = ConnectorFactory.getRemoteProtoServer(address);

    RemoteWriteReadReentrantLock remoteLock = new RemoteWriteReadReentrantLock(remoteConnector);
    remoteLock.readLock().lock();
    System.out.println("Thread.currentThread().getId():" + Thread.currentThread().getId());

    Thread.sleep(10000);
    remoteLock.readLock().unlock();
  }
}
