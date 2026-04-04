package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.concurrent.Callable;
import dev.marshalhayes.digitalai.agility.tools.AgilityQuery;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.HtmlConverter;
import dev.marshalhayes.digitalai.agility.tools.cli.Spinner;
import dev.marshalhayes.digitalai.agility.tools.cli.mixins.HelpMixin;
import dev.marshalhayes.digitalai.agility.tools.cli.stories.ViewStoryCommand.Story;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Help.Ansi;

@Component
@Command(name = "view")
@RegisterReflectionForBinding(Story.class)
public class ViewStoryCommand implements Callable<Integer> {
  private final ObjectProvider<AgilityQueryClient> queryClientProvider;

  @Mixin
  private HelpMixin helpMixin;

  @Parameters
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  public ViewStoryCommand(ObjectProvider<AgilityQueryClient> queryClientProvider) {
    this.queryClientProvider = queryClientProvider;
  }

  @Override
  public Integer call() throws Exception {
    var queryClient = queryClientProvider.getObject();

    var query = AgilityQuery.builder()
        .from("Story")
        .where("Number", storyNumber)
        .select("Number", "Name", "Description");

    var stories = Spinner.execute(spec.commandLine().getErr(),
        () -> queryClient.query(query, Story.class));

    if (stories.isEmpty()) {
      spec.commandLine().getErr()
          .println("Story with number %s could not be found".formatted(storyNumber));

      return 1;
    }

    var story = stories.getFirst();

    var markdownDescription = HtmlConverter.convert(story.description());

    var storyDetails = """
        @|bold %s|@ - %s

        %s""".formatted(story.number(), story.name(), markdownDescription);

    spec.commandLine().getOut().println(Ansi.AUTO.string(storyDetails));

    return 0;
  }

  public record Story(
      @JsonProperty("Number") String number,
      @JsonProperty("Name") String name,
      @JsonProperty("Description") String description) {
  }
}
