package com.remote.lock.parse;

import com.remote.lock.entry.GroupProtocolEntry;
import com.remote.lock.entry.HostEntry;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ProtocolParse implements IParse {
  @Override
  public Map<String, GroupProtocolEntry> parse(String addressStr) {
    String[] protocol = addressStr.split(",");
    Map<String, GroupProtocolEntry> entryMap = new HashMap<String, GroupProtocolEntry>();
    for (String p : protocol) {
      try {
        URI uri = new URI(p);
        String key = key(uri.getScheme(), uri.getPath());
        if (!entryMap.containsKey(key)) {
          entryMap.put(
              key,
              new GroupProtocolEntry(uri.getScheme(), uri.getPath(), new ArrayList<HostEntry>()));
        }
        GroupProtocolEntry group = entryMap.get(key);
        HostEntry protocolEntry = new HostEntry();
        protocolEntry.setHost(uri.getHost());
        protocolEntry.setPort(uri.getPort());
        group.getProtocolEntries().add(protocolEntry);
      } catch (Exception e) {
        log.error(p + ":", e);
      }
    }
    return entryMap;
  }

  private String key(String protocol, String fullPath) {
    return protocol + "_" + fullPath;
  }
}
