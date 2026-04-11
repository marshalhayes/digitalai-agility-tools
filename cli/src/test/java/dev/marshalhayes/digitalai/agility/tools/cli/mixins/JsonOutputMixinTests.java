package dev.marshalhayes.digitalai.agility.tools.cli.mixins;

import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;
import tools.jackson.databind.json.JsonMapper;

class JsonOutputMixinTests {
  @Test
  void shouldUseDefaultFieldsForBareJsonFlag() {
    var command = new TestCommand();

    var exitCode = new CommandLine(command).execute("S-1001", "--json");

    assertThat(exitCode).isZero();
    assertThat(command.storyNumber).isEqualTo("S-1001");
    assertThat(command.jsonOutput.isRequested()).isTrue();
    assertThat(command.jsonOutput.fieldsOrElse("Number")).containsExactly("Number");
  }

  @Test
  void shouldReturnDefaultsWhenJsonNotSpecified() {
    var command = new TestCommand();

    var exitCode = new CommandLine(command).execute("S-1001");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.isRequested()).isFalse();
    assertThat(command.jsonOutput.fieldsOrElse("Number", "Name")).containsExactly("Number", "Name");
  }

  @Test
  void shouldIgnoreBlankFieldsFromDoubleComma() {
    var command = new TestCommand();

    var exitCode = new CommandLine(command).execute("S-1001", "--json=Number,,Name");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.fieldsOrElse("Description")).containsExactly("Number", "Name");
  }

  @Test
  void shouldReturnDefaultsWhenAllFieldsAreBlank() {
    var command = new TestCommand();

    var exitCode = new CommandLine(command).execute("S-1001", "--json=,,");

    assertThat(exitCode).isZero();
    assertThat(command.jsonOutput.isRequested()).isTrue();
    assertThat(command.jsonOutput.fieldsOrElse("Number", "Name")).containsExactly("Number", "Name");
  }

  @Test
  void shouldParseCommaSeparatedJsonFields() {
    var command = new TestCommand();

    var exitCode = new CommandLine(command).execute("--json=Number,Name", "S-1001");

    assertThat(exitCode).isZero();
    assertThat(command.storyNumber).isEqualTo("S-1001");
    assertThat(command.jsonOutput.isRequested()).isTrue();
    assertThat(command.jsonOutput.fieldsOrElse("Description")).containsExactly("Number", "Name");
  }

  @Command(name = "view")
  static class TestCommand implements Callable<Integer> {
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
