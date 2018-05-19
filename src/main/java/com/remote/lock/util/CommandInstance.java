package com.remote.lock.util;

import com.remote.lock.command.CommandEnum;
import com.remote.lock.command.CommandExec;
import com.remote.lock.command.ICommand;
import com.remote.lock.command.IListener;

public final class CommandInstance {

  private CommandExec commandExec;

  private CommandInstance() {
    commandExec = new CommandExec();
  }

  private static class CommandFactory {
    private static CommandInstance Instance = new CommandInstance();
  }

  public static CommandInstance getInstance() {
    return getInstance(false);
  }

  public static CommandInstance getInstance(boolean asyn) {
    return CommandFactory.Instance;
  }

  public void invoke(ICommand command) {
    commandExec.exec(command);
  }

  public void registerProcessor(ICommand command, IListener listener) {
    commandExec.addCommand(CommandEnum.valueOf(command.key().toUpperCase()), listener);
  }

  public void clear(ICommand command) {
    commandExec.removeGroup(CommandEnum.valueOf(command.key().toUpperCase()));
  }
}
