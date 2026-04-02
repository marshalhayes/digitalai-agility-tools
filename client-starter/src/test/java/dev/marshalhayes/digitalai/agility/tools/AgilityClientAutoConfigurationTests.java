package dev.marshalhayes.digitalai.agility.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class AgilityClientAutoConfigurationTests {
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
      .withConfiguration(AutoConfigurations.of(AgilityClientAutoConfiguration.class))
      .withPropertyValues("agility.url=http://localhost", "agility.access-token=tokenValue");

  @Test
  void shouldLoadAgilityQueryClient() {
    contextRunner.run(context -> {
      var client = context.getBean(AgilityQueryClient.class);

      assertThat(client)
          .isNotNull()
          .isInstanceOf(DefaultAgilityQueryClient.class);
    });
  }

  @Test
  void shouldUseProvidedConfigValues() {
    contextRunner.run(context -> {
      var config = context.getBean(AgilityClientConfigurationProperties.class);

      assertThat(config)
          .isNotNull();

      assertThat(config.url())
        .isEqualTo("http://localhost");

      assertThat(config.accessToken())
          .isEqualTo("tokenValue");
    });
  }
}
