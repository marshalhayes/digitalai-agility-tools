package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.concurrent.Callable;

import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.HelpMixin;
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
import tools.jackson.databind.ObjectMapper;

@Component
@Command(name = "view")
@RegisterReflectionForBinding(Story.class)
public class ViewStoryCommand implements Callable<Integer> {
  private final ObjectProvider<AgilityQueryClient> queryClientProvider;

  private final ObjectMapper objectMapper;

  @Mixin
  private HelpMixin helpMixin;

  @Parameters
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  public ViewStoryCommand(ObjectProvider<AgilityQueryClient> queryClientProvider, ObjectMapper objectMapper) {
    this.queryClientProvider = queryClientProvider;
    this.objectMapper = objectMapper;
  }

  @Override
  public Integer call() throws Exception {
    var queryClient = queryClientProvider.getObject();

    var query = queryClient.from("Story")
        .where("Number", storyNumber)
        .select("Number", "Name", "Description");

    var stories = queryClient.query(query, Story.class);

    if (stories.isEmpty()) {
      spec.commandLine().getErr()
          .println("Story with number %s could not be found".formatted(storyNumber));

      return 1;
    }

    // Write the story as pretty-printed JSON to the console
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(spec.commandLine().getOut(), stories.get(0));

    return 0;
  }

  public record Story(
      @JsonProperty("Number") String number,
      @JsonProperty("Name") String name,
      @JsonProperty("Description") String description) {
  }
}
