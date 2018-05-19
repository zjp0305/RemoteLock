package com.remote.lock.command;

import lombok.Getter;

@Getter
public enum CommandEnum {
  DELETE("delete");
  private String key;

  private CommandEnum(String key) {
    this.key = key;
  }
}
