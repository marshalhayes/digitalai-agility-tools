package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import dev.marshalhayes.digitalai.agility.tools.AgilityQuery;
import dev.marshalhayes.digitalai.agility.tools.AgilityQueryClient;
import dev.marshalhayes.digitalai.agility.tools.cli.HtmlConverter;
import dev.marshalhayes.digitalai.agility.tools.cli.Spinner;
import dev.marshalhayes.digitalai.agility.tools.cli.mixins.HelpMixin;
import dev.marshalhayes.digitalai.agility.tools.cli.mixins.JsonOutputMixin;
import tools.jackson.core.type.TypeReference;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Component
@Command(name = "view")
public class ViewStoryCommand implements Callable<Integer> {
  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
  };

  private final ObjectProvider<AgilityQueryClient> queryClientProvider;

  @Mixin
  private HelpMixin helpMixin;

  @Mixin
  private JsonOutputMixin jsonOutput;

  @Parameters
  private String storyNumber;

  @Spec
  private CommandSpec spec;

  public ViewStoryCommand(ObjectProvider<AgilityQueryClient> queryClientProvider) {
    this.queryClientProvider = queryClientProvider;
  }

  @Override
  public Integer call() throws Exception {
    var story = fetchStory(jsonOutput.fieldsOrElse("Number", "Name", "Description"));

    if (story.isEmpty()) {
      spec.commandLine().getErr()
          .println("Story with number %s could not be found".formatted(storyNumber));

      return 1;
    }

    if (jsonOutput.isRequested()) {
      jsonOutput.printJson(story.getFirst());
    } else {
      printFormatted(story.getFirst());
    }

    return 0;
  }

  private void printFormatted(Map<String, Object> story) {
    var description = HtmlConverter.convert((String) story.get("Description"));

    spec.commandLine().getOut()
        .println(Ansi.AUTO.string("""
            @|bold %s|@ - %s

            %s""".formatted(story.get("Number"), story.get("Name"), description)));
  }

  private List<Map<String, Object>> fetchStory(Object... fields) throws Exception {
    var queryClient = queryClientProvider.getObject();

    var query = AgilityQuery.builder()
        .from("Story")
        .where("Number", storyNumber)
        .select(fields)
        .build();

    return Spinner.execute(spec.commandLine().getErr(),
        () -> queryClient.query(query, MAP_TYPE));
  }
}
