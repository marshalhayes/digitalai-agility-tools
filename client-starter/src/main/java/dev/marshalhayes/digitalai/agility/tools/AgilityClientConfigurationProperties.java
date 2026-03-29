package dev.marshalhayes.digitalai.agility.tools;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("agility")
public class AgilityClientConfigurationProperties {
  private String url;
  private String accessToken;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
