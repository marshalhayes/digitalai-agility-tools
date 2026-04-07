package dev.marshalhayes.digitalai.agility.tools;

import java.util.List;

import tools.jackson.core.type.TypeReference;

public interface AgilityQueryClient {
  <T> List<T> query(AgilityQuery query, TypeReference<T> type);

  <T> List<T> query(AgilityQuery query, Class<T> type);
}
