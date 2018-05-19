package com.remote.lock.connector;

import lombok.Getter;

@Getter
public enum RemoteProtocol {
  ZK("ZK", ZKConnector.class),
  ZKC("ZKC", ZKClientConnector.class);
  private String proto;
  private Class<? extends RemoteConnector> cla;

  private RemoteProtocol(String proto, Class<? extends RemoteConnector> cla) {
    this.proto = proto;
    this.cla = cla;
  }

  public RemoteConnector newInstance() {
    try {
      return this.cla.newInstance();
    } catch (Exception e) {
      return null;
    }
  }
}
