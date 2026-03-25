package dev.marshalhayes.digitalai.agility.tools.cli;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "agility", mixinStandardHelpOptions = true, versionProvider = VersionProvider.class)
public class RootCommand {
}
