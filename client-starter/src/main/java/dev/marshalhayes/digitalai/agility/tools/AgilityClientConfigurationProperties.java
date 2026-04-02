package dev.marshalhayes.digitalai.agility.tools;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("agility")
public record AgilityClientConfigurationProperties(String url, String accessToken) {}
