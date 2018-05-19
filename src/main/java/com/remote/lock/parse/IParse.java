package com.remote.lock.parse;

import com.remote.lock.entry.GroupProtocolEntry;

import java.util.Map;

public interface IParse {
  Map<String, GroupProtocolEntry> parse(String proto);
}
