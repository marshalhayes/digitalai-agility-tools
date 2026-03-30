package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.concurrent.Callable;

import dev.marshalhayes.digitalai.agility.tools.AgilityQuery;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.stories.ViewStoryCommand.Story;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "view", mixinStandardHelpOptions = true)
@RegisterReflectionForBinding(Story.class)
public class ViewStoryCommand implements Callable<Integer> {
  private final AgilityQueryClient queryClient;
  private final StoryRenderer storyRenderer;

  @Parameters(description = "The story number, e.g. S-12345")
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  public ViewStoryCommand(AgilityQueryClient queryClient, StoryRenderer storyRenderer) {
    this.queryClient = queryClient;
    this.storyRenderer = storyRenderer;
  }

  @Override
  public Integer call() throws Exception {
    var query = AgilityQuery.from("Story")
        .where("Number", storyNumber)
        .select(Story.class);

    var stories = queryClient.query(query);

    if (stories.isEmpty()) {
      spec.commandLine().getErr()
          .println("Story with number %s could not be found".formatted(storyNumber));

      return 1;
    }

    var story = stories.get(0);

    try (var writer = spec.commandLine().getOut()) {
      var storyView = new StoryView(story.number(), story.name(), story.description());

      storyRenderer.render(storyView, writer);
    }

    return 0;
  }

  static record Story(
      String number,
      String name,
      String description) {
  };
}
