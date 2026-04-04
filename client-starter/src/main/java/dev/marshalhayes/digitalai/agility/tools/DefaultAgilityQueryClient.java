package dev.marshalhayes.digitalai.agility.tools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

public class DefaultAgilityQueryClient implements AgilityQueryClient {
  private static final Logger log = LoggerFactory.getLogger(DefaultAgilityQueryClient.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public DefaultAgilityQueryClient(RestClient restClient, ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public <T> List<T> query(AgilityQuery query, Class<T> type) {
    var queryJson = objectMapper.writeValueAsString(query);

    log.debug("Sending query to /query.v1: {}", queryJson);

    var response = restClient.post()
        .uri("/query.v1")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(queryJson)
        .retrieve()
        .body(String.class);

    log.debug("Response from /query.v1: {}", response);

    var root = objectMapper.readTree(response);
    var resultSet = (!root.isEmpty() && root.get(0) != null) ? root.get(0) : objectMapper.createArrayNode();

    if (!resultSet.isArray()) {
      return List.of();
    }

    var results = new ArrayList<T>(resultSet.size());

    for (var asset : resultSet) {
      results.add(objectMapper.convertValue(asset, type));
    }

    return List.copyOf(results);
  }
}
