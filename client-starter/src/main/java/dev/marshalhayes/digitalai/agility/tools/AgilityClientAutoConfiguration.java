package dev.marshalhayes.digitalai.agility.tools;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableConfigurationProperties(AgilityClientConfigurationProperties.class)
public class AgilityClientAutoConfiguration {
  @Bean
  @Lazy
  RestClient agilityRestClient(AgilityClientConfigurationProperties config) {
    Assert.hasText(config.url(), "Agility url is required");
    Assert.hasText(config.accessToken(), "Agility access token is required");

    return RestClient.builder()
        .baseUrl(config.url())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(config.accessToken()))
        .build();
  }

  @Bean
  @Lazy
  AgilityQueryClient agilityQueryClient(RestClient agilityRestClient, ObjectProvider<ObjectMapper> objectMapper) {
    return new DefaultAgilityQueryClient(agilityRestClient, objectMapper.getIfAvailable(ObjectMapper::new));
  }
}
