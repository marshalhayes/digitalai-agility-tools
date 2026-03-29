package dev.marshalhayes.digitalai.agility.tools;

import java.util.List;

public interface AgilityQueryClient {
  <T> List<T> query(AgilityQuery<T> query, Class<T> type);

  <T> List<T> query(AgilityQuery<T> query);
}
