package com.remote.lock.connector;

import com.remote.lock.entry.GroupProtocolEntry;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public abstract class CommonConnector implements RemoteConnector {
  protected GroupProtocolEntry entry;

  protected int timeout = 2000;

  protected String prefix;

  public GroupProtocolEntry getProtocol() {
    return entry;
  };

  @Override
  public void setProtocol(GroupProtocolEntry entry) {
    this.entry = entry;
  }

  private KeepAlive keepAlive = new KeepAlive();

  public abstract void keepAlive(String childrenNode);

  protected void startKeepAlive() {
    keepAlive.start();
  }

  protected void stopKeepAlive() {
    keepAlive.close();
  }

  class KeepAlive implements Runnable {

    private AtomicBoolean isStart = new AtomicBoolean(false);

    private ScheduledExecutorService service =
        Executors.newScheduledThreadPool(
            1,
            new ThreadFactory() {
              @Override
              public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                thread.setName("KeepAlive");
                return thread;
              }
            });

    public synchronized void start() {
      if (isStart.compareAndSet(false, true)) {
        service.scheduleAtFixedRate(this, 0l, 2000, MILLISECONDS);
      }
    }

    @Override
    public void run() {
      try {
        keepAlive("/");
      } catch (Exception e) {

      }
    }

    public void close() {
      isStart.compareAndSet(true, false);
      service.shutdown();
    }
  }
}
