package dev.marshalhayes.digitalai.agility.tools;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DefaultAgilityQueryClientTests {
  private DefaultAgilityQueryClient client;
  private MockRestServiceServer server;

  private static final ObjectMapper objectMapper = JsonMapper.builder().build();
  private static final String RESPONSE_JSON = """
      [[{"Number":"S-1001","Name":"Test Story"}]]
      """;

  @BeforeEach
  void setup() {
    var restClientBuilder = RestClient.builder().baseUrl("http://localhost");

    this.server = MockRestServiceServer.bindTo(restClientBuilder).build();
    this.client = new DefaultAgilityQueryClient(restClientBuilder.build(), objectMapper);
  }

  @Test
  void shouldDeserializeToProjectionClass() {
    server.expect(requestTo("http://localhost/query.v1"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(RESPONSE_JSON, MediaType.APPLICATION_JSON));

    var query = AgilityQuery.builder().from("Story").select("Number", "Name").build();

    var results = client.query(query, new TypeReference<Story>() {});

    assertThat(results)
        .hasSize(1)
        .first()
        .satisfies(story -> {
          assertThat(story.Number()).isEqualTo("S-1001");
          assertThat(story.Name()).isEqualTo("Test Story");
        });

    server.verify();
  }

  @Test
  void shouldDeserializeToMap() {
    server.expect(requestTo("http://localhost/query.v1"))
        .andExpect(method(HttpMethod.POST))
        .andRespond(withSuccess(RESPONSE_JSON, MediaType.APPLICATION_JSON));

    var query = AgilityQuery.builder().from("Story").select("Number", "Name").build();

    var results = client.query(query, new TypeReference<Map<String, Object>>() {});

    assertThat(results)
        .hasSize(1)
        .first()
        .satisfies(story -> {
          assertThat(story.get("Number")).isEqualTo("S-1001");
          assertThat(story.get("Name")).isEqualTo("Test Story");
        });

    server.verify();
  }

  record Story(String Number, String Name) {}
}
