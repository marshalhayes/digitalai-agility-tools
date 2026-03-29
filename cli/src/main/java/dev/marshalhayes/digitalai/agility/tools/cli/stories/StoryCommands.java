package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(name = "story", subcommands = ViewStoryCommand.class, mixinStandardHelpOptions = true)
public class StoryCommands {
}
