package com.remote.lock.command;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;

public class CommandExec {
  private ConcurrentHashMap<String, ListenerGroup> groupEventListener = new ConcurrentHashMap();

  public void exec(ICommand ICommand) {
    ListenerGroup group = groupEventListener.get(ICommand.key());
    if (nonNull(group)) {
      group.notifyListener();
    }
  }

  public void addCommand(CommandEnum command, IListener listener) {
    if (!groupEventListener.containsKey(command.getKey())) {
      groupEventListener.put(command.getKey(), new ListenerGroup());
    }
    ListenerGroup group = groupEventListener.get(command.getKey());
    group.add(listener);
  }

  public void removeGroup(CommandEnum command) {
    ListenerGroup group = groupEventListener.remove(command.getKey());
    if (nonNull(group)) {
      group.clear();
    }
  }
}
