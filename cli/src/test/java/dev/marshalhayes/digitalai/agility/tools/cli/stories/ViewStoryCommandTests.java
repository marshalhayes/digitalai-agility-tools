package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.marshalhayes.digitalai.agility.tools.cli.mixins.HelpMixin;
import dev.marshalhayes.digitalai.agility.tools.cli.mixins.JsonOutputMixin;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;
import tools.jackson.databind.json.JsonMapper;

class ViewStoryCommandTests {
  @Test
  void shouldAcceptStoryNumberParameter() {
    var command = new TestViewCommand();

    var exitCode = new CommandLine(command).execute("S-1001");

    assertThat(exitCode).isZero();
    assertThat(command.storyNumber).isEqualTo("S-1001");
  }

  @Test
  void shouldSupportJsonFlagForFieldSelection() {
    var command = new TestViewCommand();

    var exitCode = new CommandLine(command).execute("S-1001", "--json=Name");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.isRequested()).isTrue();
  }

  @Test
  void shouldSupportCommaSeparatedFieldSelection() {
    var command = new TestViewCommand();

    var exitCode = new CommandLine(command).execute("S-1001", "--json=Number,Name");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.fieldsOrElse("Description")).containsExactly("Number", "Name");
  }

  @Test
  void shouldUseDefaultFieldsWhenJsonFlagOmitted() {
    var command = new TestViewCommand();

    var exitCode = new CommandLine(command).execute("S-1001");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.isRequested()).isFalse();
    assertThat(command.jsonOutput.fieldsOrElse("Number", "Name", "Description"))
        .containsExactly("Number", "Name", "Description");
  }

  @Command(name = "view")
  static class TestViewCommand implements Callable<Integer> {
    @Mixin
    private HelpMixin helpMixin;

    @Mixin
    private JsonOutputMixin jsonOutput = new JsonOutputMixin(JsonMapper.builder().build());

    @Parameters
    private String storyNumber;

    @Override
    public Integer call() {
      return 0;
    }
  }
}
