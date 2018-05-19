package com.remote.lock;

import com.remote.lock.connector.RemoteConnector;
import com.remote.lock.entry.GroupProtocolEntry;
import com.remote.lock.util.FilterUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

@Slf4j
public abstract class RemoteLock implements Lock {

  protected RemoteConnector remoteConnector;

  protected ThreadLocal<LockNode> lockNode = new ThreadLocal();

  protected String prefix = "lock_";

  protected List<String> sort(List<String> children) {
    Collections.sort(
        children,
        new Comparator<String>() {
          @Override
          public int compare(String o1, String o2) {
            int v1 = Integer.valueOf(o1.substring(o1.lastIndexOf("_") + 1));
            int v2 = Integer.valueOf(o2.substring(o2.lastIndexOf("_") + 1));
            if (v1 > v2) {
              return 1;
            } else if (v1 < v2) {
              return -1;
            }
            return 0;
          }
        });
    return children;
  }

  protected List<String> filter(List<String> children, String... prefix) {
    String[] array = prefix.clone();
    return FilterUtil.filter(
        children,
        new FilterUtil.FilterCondition<String>() {
          @Override
          public boolean condition(String s) {
            boolean isOk = false;
            for (int i = 0; i < array.length; i++) {
              isOk = s.indexOf(array[i]) != -1;
              if (isOk) {
                break;
              }
            }
            return isOk;
          }
        });
  }

  protected int getIndex(List<String> children) {
    String nodeName = lockNode.get().getNodeName();
    return children.indexOf(nodeName.substring(nodeName.lastIndexOf("/") + 1));
  }

  protected boolean holdLock() throws Exception {
    GroupProtocolEntry entry = remoteConnector.getProtocol();
    List<String> children = remoteConnector.getChildren(entry.getPath());
    children = filter(children, prefix);
    sort(children);
    int index = getIndex(children);
    if (index < 0) {
      throw new RuntimeException("Node not Found");
    }
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

  private boolean remoteLockReal(WaitStrategy waitStrategy) throws Exception {
    String lockNode = remoteConnector.createNode(prefix);
    this.lockNode.set(new LockNode(lockNode));
    while (true) {
      try {
        boolean holdLock = holdLock();
        if (!holdLock) {
          boolean isContinue = waitStrategy.waitStrategy();
          if (isContinue) {
            continue;
          }
          return false;
        }
        return true;
      } catch (Exception e) {
        throw e;
      }
    }
  }

  protected boolean remoteLock(WaitStrategy waitStrategy) {
    try {
      this.localLock();
      LockNode node = this.lockNode.get();
      if (node != null) {
        node.getLockHoldCount().incrementAndGet();
        return true;
      }
      return remoteLockReal(waitStrategy);
    } catch (Exception e) {
      log.error("lock error:", e);
    } finally {
      this.localUnLock();
    }
    return true;
  }

  @Override
  public void unlock() {
    try {
      this.localLock();
      LockNode node = this.lockNode.get();
      if (node != null) {
        node.getLockHoldCount().decrementAndGet();
      }
      if (node.getLockHoldCount().get() == 0) {
        remoteConnector.deleteNode(node.getNodeName());
      }

    } catch (Exception e) {
      log.error("unlock error:", e);
    } finally {
      this.localUnLock();
    }
  }

  public abstract void localLock();

  public abstract void localUnLock();

  interface WaitStrategy {
    boolean waitStrategy() throws InterruptedException;
  }

  @Getter
  @Setter
  class LockNode {
    private String nodeName;

    private AtomicInteger lockHoldCount = new AtomicInteger(1);

    public LockNode(String nodeName) {
      this.nodeName = nodeName;
    }
  }
}
