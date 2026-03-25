package dev.marshalhayes.digitalai.agility.tools.cli;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import picocli.CommandLine.IVersionProvider;

@Component
public class VersionProvider implements IVersionProvider {
  private final BuildProperties buildProperties;

  public VersionProvider(BuildProperties buildProperties) {
    this.buildProperties = buildProperties;
  }

  @Override
  public String[] getVersion() throws Exception {
    return new String[] { buildProperties.getVersion() };
  }
}
