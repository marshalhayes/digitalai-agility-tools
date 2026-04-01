package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ajalt.mordant.terminal.Terminal;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.Named;
import dev.marshalhayes.digitalai.agility.tools.cli.OutputOptions;
import dev.marshalhayes.digitalai.agility.tools.cli.SpinnerAnimation;
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
  private final Terminal terminal;
  private static final ObjectMapper OUTPUT_MAPPER = JsonMapper.builder().build();

  @Parameters(description = "The story number, e.g. S-12345")
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  @Mixin
  private OutputOptions outputOptions;

  public ViewStoryCommand(AgilityQueryClient queryClient, StoryRenderer storyRenderer,
      Terminal terminal) {
    this.queryClient = queryClient;
    this.storyRenderer = storyRenderer;
    this.terminal = terminal;
  }

  @Override
  public Integer call() throws Exception {
    var query = queryClient.from("Story")
        .where("Number", storyNumber)
        .select(Story.class);

    List<Story> stories;
    if (outputOptions.isJson()) {
      stories = queryClient.query(query);
    } else {
      try (var spinner = SpinnerAnimation.start(terminal, "Loading " + storyNumber + "...")) {
        stories = queryClient.query(query);
      }
    }

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

    var storyView = new StoryView(
        story.number(),
        story.name(),
        story.description(),
        firstName(story.status()),
        firstName(story.priority()),
        story.estimate() != null ? story.estimate().toString() : null,
        firstName(story.timebox()),
        firstName(story.scope()),
        story.owners() != null
            ? story.owners().stream().map(Named::name).collect(Collectors.joining(", "))
            : null);

    storyRenderer.render(storyView);
    return 0;
  }

  /** Returns the name of the first element in a relation list, or null if absent/empty. */
  private static String firstName(List<Named> relation) {
    return (relation != null && !relation.isEmpty()) ? relation.get(0).name() : null;
  }

  static record Story(
      @JsonProperty("Number") String number,
      @JsonProperty("Name") String name,
      @JsonProperty("Description") String description,
      @JsonProperty("Status") List<Named> status,
      @JsonProperty("Priority") List<Named> priority,
      @JsonProperty("Estimate") Double estimate,
      @JsonProperty("Timebox") List<Named> timebox,
      @JsonProperty("Scope") List<Named> scope,
      @JsonProperty("Owners") List<Named> owners) {
  }
}
