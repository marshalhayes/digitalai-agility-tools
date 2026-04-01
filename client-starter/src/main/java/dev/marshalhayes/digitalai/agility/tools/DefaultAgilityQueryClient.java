package dev.marshalhayes.digitalai.agility.tools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
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
  public AgilityQuery.Builder from(String assetType) {
    return new AgilityQuery.Builder(assetType, objectMapper);
  }

  private String execute(String queryJson) {
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

  @Override
  public <T> List<T> query(AgilityQuery<T> query, Class<T> type) {
    var responseJson = execute(serialize(query));

    var resultSet = firstResultSet(responseJson);

    var listType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, type);

    return objectMapper.convertValue(resultSet, listType);
  }

  @Override
  public <T> List<T> query(AgilityQuery<T> query) {
    var responseJson = execute(serialize(query));

    return deserializeResultSet(firstResultSet(responseJson), query);
  }

  private JsonNode firstResultSet(String responseJson) {
    var root = readTree(responseJson);

    if (root.isEmpty()) {
      return objectMapper.createArrayNode();
    }

    var resultSet = root.get(0);

    return resultSet == null ? objectMapper.createArrayNode() : resultSet;
  }

  private JsonNode readTree(String responseJson) {
    return objectMapper.readTree(responseJson);
  }

  private String serialize(AgilityQuery<?> query) {
    return objectMapper.writeValueAsString(query);
  }

  private <T> List<T> deserializeResultSet(JsonNode resultSet, AgilityQuery<T> query) {
    if (resultSet == null || !resultSet.isArray()) {
      return List.of();
    }

    var results = new ArrayList<T>(resultSet.size());

    for (var asset : resultSet) {
      results.add(query.map(asset));
    }

    return List.copyOf(results);
  }
}
