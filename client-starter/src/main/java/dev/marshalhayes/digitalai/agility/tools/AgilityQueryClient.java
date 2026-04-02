package dev.marshalhayes.digitalai.agility.tools;

import java.util.List;

public interface AgilityQueryClient {
  <T> List<T> query(AgilityQuery<T> query);

  AgilityQuery.Builder from(String assetType);
}
