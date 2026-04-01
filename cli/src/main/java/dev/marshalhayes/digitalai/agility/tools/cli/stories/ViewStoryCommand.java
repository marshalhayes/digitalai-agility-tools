package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.Named;
import dev.marshalhayes.digitalai.agility.tools.cli.OutputOptions;
import dev.marshalhayes.digitalai.agility.tools.cli.stories.ViewStoryCommand.Story;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Component
@Command(name = "view", mixinStandardHelpOptions = true)
@RegisterReflectionForBinding(Story.class)
public class ViewStoryCommand implements Callable<Integer> {

  private final AgilityQueryClient queryClient;
  private final StoryRenderer storyRenderer;
  private static final ObjectMapper OUTPUT_MAPPER = JsonMapper.builder().build();

  @Parameters(description = "The story number, e.g. S-12345")
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  @Mixin
  private OutputOptions outputOptions;

  public ViewStoryCommand(AgilityQueryClient queryClient, StoryRenderer storyRenderer) {
    this.queryClient = queryClient;
    this.storyRenderer = storyRenderer;
  }

  @Override
  public Integer call() throws Exception {
    var query = queryClient.from("Story")
        .where("Number", storyNumber)
        .select(Story.class);

    var stories = queryClient.query(query);

    if (stories.isEmpty()) {
      spec.commandLine().getErr()
          .println("Story with number %s could not be found".formatted(storyNumber));
      return 1;
    }

    var story = stories.get(0);

    if (outputOptions.isJson()) {
      spec.commandLine().getOut()
          .println(OUTPUT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(story));
      return 0;
    }

    try (var writer = spec.commandLine().getOut()) {
      var storyView = new StoryView(
          story.number(),
          story.name(),
          story.description(),
          story.status() != null ? story.status().name() : null,
          story.priority() != null ? story.priority().name() : null,
          story.estimate() != null ? story.estimate().toString() : null,
          story.timebox() != null ? story.timebox().name() : null,
          story.scope() != null ? story.scope().name() : null,
          story.owners() != null
              ? story.owners().stream().map(Named::name).collect(Collectors.joining(", "))
              : null);

      storyRenderer.render(storyView, writer);
    }

    return 0;
  }

  static record Story(
      @JsonProperty("Number") String number,
      @JsonProperty("Name") String name,
      @JsonProperty("Description") String description,
      @JsonProperty("Status") Named status,
      @JsonProperty("Priority") Named priority,
      @JsonProperty("Estimate") Double estimate,
      @JsonProperty("Timebox") Named timebox,
      @JsonProperty("Scope") Named scope,
      @JsonProperty("Owners") List<Named> owners) {
  }
}
