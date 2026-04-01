package dev.marshalhayes.digitalai.agility.tools.cli;

import picocli.CommandLine.Option;

/** Picocli mixin that adds a {@code --json} output flag to any command that includes it. */
public class OutputOptions {
  @Option(names = "--json", description = "Output as JSON instead of formatted text")
  private boolean json;

  public boolean isJson() {
    return json;
  }
}
