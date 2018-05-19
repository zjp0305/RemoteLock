package com.remote.lock.command;

import java.util.ArrayList;
import java.util.List;

public class ListenerGroup {
  private List<IListener> group = new ArrayList<>();

  public void add(IListener listener) {
    group.add(listener);
  }

  public void notifyListener() {
    for (IListener listener : group) {
      listener.listener();
    }
  }

  public void clear() {
    group.clear();
  }
}
