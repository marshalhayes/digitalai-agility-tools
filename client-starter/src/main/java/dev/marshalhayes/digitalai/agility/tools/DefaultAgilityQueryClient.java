package dev.marshalhayes.digitalai.agility.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

public class DefaultAgilityQueryClient implements AgilityQueryClient {
  private static final Logger log = LoggerFactory.getLogger(DefaultAgilityQueryClient.class);

  private final RestClient restClient;
  private final ObjectMapper objectMapper;

  public DefaultAgilityQueryClient(RestClient restClient, ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public <T> List<T> query(AgilityQuery query, TypeReference<T> type) {
    return parseResults(post(query), query.getSelect(), asset -> objectMapper.convertValue(asset, type));
  }

  @Override
  public <T> List<T> query(AgilityQuery query, Class<T> type) {
    return parseResults(post(query), query.getSelect(), asset -> objectMapper.convertValue(asset, type));
  }

  private String post(AgilityQuery query) {
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

    return response;
  }

  private <T> List<T> parseResults(String response, List<Object> selectedFields, Function<JsonNode, T> converter) {
    var root = objectMapper.readTree(response);
    var resultSet = (!root.isEmpty() && root.get(0) != null) ? root.get(0) : objectMapper.createArrayNode();

    if (!resultSet.isArray()) {
      return List.of();
    }

    var results = new ArrayList<T>(resultSet.size());

    var keepFields = selectedFields.stream()
        .filter(f -> f instanceof String)
        .map(Object::toString)
        .collect(java.util.stream.Collectors.toSet());

    for (var asset : resultSet) {
      if (!keepFields.isEmpty()) {
        ((ObjectNode) asset).retain(keepFields);
      }
      results.add(converter.apply(asset));
    }

    return List.copyOf(results);
  }
}
