package com.remote.lock.util;

import java.util.List;
import java.util.stream.Collectors;

public final class FilterUtil {

  public static <T> List<T> filter(List<T> t, FilterCondition<T> condition) {
    return t.stream()
        .filter(
            (p) -> {
              return condition.condition(p);
            })
        .collect(Collectors.toList());
  }

  public interface FilterCondition<T> {
    boolean condition(T t);
  }
}
