package dev.marshalhayes.digitalai.agility.tools.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class ConfigFileProcessor implements EnvironmentPostProcessor {
  private static final Logger log = LoggerFactory.getLogger(ConfigFileProcessor.class);
  private static final Path CONFIG_PATH = Path.of(System.getProperty("user.home"), ".agility", "config.json");

  private static final Map<String, String> KEY_MAP = Map.of(
      "url", "agility.url",
      "accessToken", "agility.access-token");

  @Override
  public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
    if (!Files.exists(CONFIG_PATH)) {
      log.debug("Config file {} does not exist", CONFIG_PATH);

      return;
    }

    try {
      var content = Files.readString(CONFIG_PATH);

      var root = JsonParserFactory.getJsonParser()
          .parseMap(content);

      var map = KEY_MAP.entrySet()
          .stream()
          .filter(entry -> root.containsKey(entry.getKey()))
          .collect(Collectors.toUnmodifiableMap(Map.Entry::getValue, entry -> root.get(entry.getKey())));

      environment.getPropertySources()
          .addLast(new MapPropertySource("agilityConfig", map));

    } catch (IOException | RuntimeException e) {
      log.debug("Failed to read {}", CONFIG_PATH, e);
    }
  }
}
