package com.remote.lock;

import com.remote.lock.command.CommandEnum;
import com.remote.lock.command.ICommand;
import com.remote.lock.command.IListener;
import com.remote.lock.connector.RemoteConnector;
import com.remote.lock.util.CommandInstance;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RemoteReentrantLock extends RemoteLock implements Lock {

  private ReentrantLock localReentrantLock;

  private Condition condition;

  public RemoteReentrantLock(RemoteConnector remoteConnector) throws Exception {
    this.remoteConnector = remoteConnector;
    this.localReentrantLock = new ReentrantLock();
    this.condition = this.localReentrantLock.newCondition();
    CommandInstance.getInstance()
        .registerProcessor(
            new ICommand() {
              @Override
              public String key() {
                return CommandEnum.DELETE.getKey();
              }
            },
            new IListener() {

              @Override
              public void listener() {
                try {
                  localLock();
                  condition.signalAll();
                } catch (Exception e) {
                  log.error("notify", e);
                } finally {
                  localUnLock();
                }
              }
            });
  }

  @Override
  public void localLock() {
    this.localReentrantLock.lock();
  }

  @Override
  public void localUnLock() {
    this.localReentrantLock.unlock();
  }

  @Override
  public void lock() {
    remoteLock(new LockWaitStrategy());
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    if (!Thread.interrupted()) {
      remoteLock(new InterruptedWaitStrategy(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
      return;
    }
    throw new InterruptedException();
  }

  @Override
  public boolean tryLock() {
    return remoteLock(new TryLockWaitStrategy());
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    if (!Thread.interrupted()) {
      return remoteLock(new InterruptedWaitStrategy(time, unit));
    }
    throw new InterruptedException();
  }

  @Override
  public Condition newCondition() {
    throw new UnsupportedOperationException();
    // return new RemoteCondition(this.remoteConnector, this);
  }

  @AllArgsConstructor
  class InterruptedWaitStrategy implements WaitStrategy {
    private long time;

    private TimeUnit unit;

    @Override
    public boolean waitStrategy() throws InterruptedException {
      condition.await();
      return true;
    }
  }

  class TryLockWaitStrategy implements WaitStrategy {
    @Override
    public boolean waitStrategy() {
      return false;
    }
  }

  class LockWaitStrategy implements WaitStrategy {

    @Override
    public boolean waitStrategy() {
      condition.awaitUninterruptibly();
      return true;
    }
  }
}
