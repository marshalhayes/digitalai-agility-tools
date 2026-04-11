package dev.marshalhayes.digitalai.agility.tools.cli.mixins;

import picocli.CommandLine.Option;

public class HelpMixin {
  @Option(names = {"-h", "--help"}, usageHelp = true)
  private boolean helpRequested;
}
