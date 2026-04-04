package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import org.springframework.stereotype.Component;

import dev.marshalhayes.digitalai.agility.tools.cli.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Component
@Command(name = "story", subcommands = ViewStoryCommand.class)
public class StoryCommands {
  @Mixin
  private HelpMixin helpMixin;
}
