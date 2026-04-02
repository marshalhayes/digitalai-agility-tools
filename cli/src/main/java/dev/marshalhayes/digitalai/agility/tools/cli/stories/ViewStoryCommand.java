package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.github.ajalt.mordant.terminal.Terminal;
import dev.marshalhayes.digitalai.agility.tools.AgilityClientConfigurationProperties;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.OutputOptions;
import dev.marshalhayes.digitalai.agility.tools.cli.SpinnerAnimation;

import org.springframework.beans.factory.ObjectProvider;
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
@RegisterReflectionForBinding({
    Story.class,
    Story.Status.class,
    Story.Priority.class,
    Story.Timebox.class,
    Story.Scope.class,
    Story.Member.class
})
public class ViewStoryCommand implements Callable<Integer> {

  private final ObjectProvider<AgilityQueryClient> queryClientProvider;
  private final StoryRenderer storyRenderer;
  private final Terminal terminal;
  private final AgilityClientConfigurationProperties config;
  private static final ObjectMapper OUTPUT_MAPPER = JsonMapper.builder().build();

  @Parameters(description = "The story number, e.g. S-12345")
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  @Mixin
  private OutputOptions outputOptions;

  public ViewStoryCommand(ObjectProvider<AgilityQueryClient> queryClientProvider, StoryRenderer storyRenderer,
      Terminal terminal, AgilityClientConfigurationProperties config) {
    this.queryClientProvider = queryClientProvider;
    this.storyRenderer = storyRenderer;
    this.terminal = terminal;
    this.config = config;
  }

  @Override
  public Integer call() throws Exception {
    var queryClient = queryClientProvider.getObject();
    var query = queryClient.from("Story")
        .where("Number", storyNumber)
        .select(Story.class);

    List<Story> stories;
    if (outputOptions.isJson()) {
      stories = queryClient.query(query);
    } else {
      try (var _ = SpinnerAnimation.start(terminal)) {
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

    var storyUrl = config.url() + "/story.mvc/Summary?oidToken=" + story.oid();

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
            ? story.owners().stream().map(Story.Member::name).collect(Collectors.joining(", "))
            : null,
        storyUrl);

    storyRenderer.render(storyView);
    return 0;
  }
}
