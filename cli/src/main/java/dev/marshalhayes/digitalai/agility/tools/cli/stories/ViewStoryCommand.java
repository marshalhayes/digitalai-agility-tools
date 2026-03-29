package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.concurrent.Callable;

import dev.marshalhayes.digitalai.agility.tools.AgilityQuery;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.HtmlToAnsiConverter;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "view", mixinStandardHelpOptions = true)
@RegisterReflectionForBinding(ViewStoryCommand.Story.class)
public class ViewStoryCommand implements Callable<Integer> {
  private final AgilityQueryClient queryClient;
  private final HtmlToAnsiConverter htmlToAnsiConverter;

  @Parameters(description = "The story number, e.g. S-12345")
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  public ViewStoryCommand(AgilityQueryClient queryClient, HtmlToAnsiConverter htmlToAnsiConverter) {
    this.queryClient = queryClient;
    this.htmlToAnsiConverter = htmlToAnsiConverter;
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

    var out = spec.commandLine().getOut();

    out.println(Ansi.AUTO.string("@|bold %s - %s|@".formatted(story.number(), story.name())));
    out.println();
    out.println(Ansi.AUTO.string(htmlToAnsiConverter.convert(story.description())));

    return 0;
  }

  static record Story(
      String number,
      String name,
      String description) {
  };
}
