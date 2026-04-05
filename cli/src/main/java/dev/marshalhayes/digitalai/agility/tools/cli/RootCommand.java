package dev.marshalhayes.digitalai.agility.tools.cli;

import dev.marshalhayes.digitalai.agility.tools.cli.stories.StoryCommands;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
  name = "agility",
  subcommands = { StoryCommands.class, SkillCommand.class },
  mixinStandardHelpOptions = true,
  versionProvider = VersionProvider.class
)
public class RootCommand {
}
