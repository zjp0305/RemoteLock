package com.remote.lock.entry;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class GroupProtocolEntry {

  private String protocol;

  private String path;

  private List<HostEntry> protocolEntries;
}
