package dev.marshalhayes.digitalai.agility.tools;

import java.util.List;

public interface AgilityQueryClient {
  <T> List<T> query(AgilityQuery query, Class<T> type);
}
