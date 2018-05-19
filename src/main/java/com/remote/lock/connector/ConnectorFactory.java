package com.remote.lock.connector;

import com.remote.lock.entry.GroupProtocolEntry;
import com.remote.lock.parse.ProtocolParse;

import java.util.Iterator;
import java.util.Map;

public final class ConnectorFactory {

  private static ProtocolParse protoParse = new ProtocolParse();

  public static RemoteConnector getRemoteProtoServer(String addressStr) {
    Map<String, GroupProtocolEntry> groupProtocol = protoParse.parse(addressStr);
    return createRemoteProtoServer(groupProtocol);
  }

  private static RemoteConnector createRemoteProtoServer(
      Map<String, GroupProtocolEntry> groupProtocol) {
    Iterator<Map.Entry<String, GroupProtocolEntry>> iterator = groupProtocol.entrySet().iterator();
    RemoteConnector remote = null;
    while (iterator.hasNext())
      try {
        Map.Entry<String, GroupProtocolEntry> next = iterator.next();
        GroupProtocolEntry group = next.getValue();
        remote = newInstance(remote, group.getProtocol());
        remote.setProtocol(group);
        remote.connect();
      } catch (Exception e) {
        e.printStackTrace();
      }
    return remote;
  }

  private static RemoteConnector newInstance(RemoteConnector remote, String protocol) {
    if (null != remote) {
      CompositeConnector compositeServer = null;
      if (!(remote instanceof CompositeConnector)) {
        compositeServer = new CompositeConnector();
        compositeServer.addRemoteServer(remote);
      }
      compositeServer.addRemoteServer(RemoteProtocol.valueOf(protocol.toUpperCase()).newInstance());
      return compositeServer;
    }
    return RemoteProtocol.valueOf(protocol.toUpperCase()).newInstance();
  }
}
