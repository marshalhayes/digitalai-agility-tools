package dev.marshalhayes.digitalai.agility.tools;

import jakarta.annotation.PostConstruct;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
@EnableConfigurationProperties(AgilityClientConfigurationProperties.class)
@RegisterReflectionForBinding({AgilityQuery.class, AgilityQuery.PageSpec.class})
public class AgilityClientAutoConfiguration {
  @PostConstruct
  void configureAgility() {
    AgilityQuery.configure(agilityObjectMapper());
  }

  @Bean
  @Lazy
  @ConditionalOnMissingBean(name = "agilityRestClient")
  RestClient agilityRestClient(AgilityClientConfigurationProperties config) {
    Assert.hasText(config.getUrl(), "Agility url is required");
    Assert.hasText(config.getAccessToken(), "Agility access token is required");

    return RestClient.builder()
        .baseUrl(config.getUrl())
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer %s".formatted(config.getAccessToken()))
        .build();
  }

  @Bean
  @Lazy
  @ConditionalOnMissingBean(name = "agilityObjectMapper")
  ObjectMapper agilityObjectMapper() {
    return JsonMapper.builder()
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
        .build();
  }

  @Bean
  @Lazy
  @ConditionalOnMissingBean
  AgilityQueryClient agilityQueryClient(RestClient agilityRestClient, ObjectMapper agilityObjectMapper) {
    return new DefaultAgilityQueryClient(agilityRestClient, agilityObjectMapper);
  }
}
