package com.remote.lock;

import com.remote.lock.connector.RemoteConnector;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RemoteCondition implements Condition {
  protected RemoteConnector remoteConnector;

  private RemoteReentrantLock remoteReentrantLock;

  protected String prefix;

  private ReentrantLock localReentrantLock;

  private Condition condition;

  public RemoteCondition(RemoteConnector remoteConnector, RemoteReentrantLock remoteReentrantLock) {
    this.remoteConnector = remoteConnector;
    this.remoteReentrantLock = remoteReentrantLock;
    this.localReentrantLock = new ReentrantLock();
    this.condition = this.localReentrantLock.newCondition();
    this.prefix = "cond_";
  }

  @Override
  public void await() throws InterruptedException {}

  @Override
  public void awaitUninterruptibly() {}

  @Override
  public long awaitNanos(long nanosTimeout) throws InterruptedException {
    if (Thread.interrupted()) {
      throw new InterruptedException();
    }

    localReentrantLock.lock();
    try {
      remoteConnector.createNode(this.prefix);
      while (true) {
        if (Thread.interrupted()) {
          throw new InterruptedException();
        }
      }

    } catch (Exception e) {

    }
    localReentrantLock.unlock();
    return 0;
  }

  @Override
  public boolean await(long time, TimeUnit unit) throws InterruptedException {
    return false;
  }

  @Override
  public boolean awaitUntil(Date deadline) throws InterruptedException {
    return false;
  }

  @Override
  public void signal() {}

  @Override
  public void signalAll() {}
}
