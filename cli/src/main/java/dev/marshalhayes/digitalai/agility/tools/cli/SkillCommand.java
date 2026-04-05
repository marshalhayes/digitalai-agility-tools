package dev.marshalhayes.digitalai.agility.tools.cli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Component
@Command(name = "skill", description = "Print the skill file for use by AI agents")
@ImportRuntimeHints(SkillCommand.Hints.class)
public class SkillCommand implements Callable<Integer> {

  private static final String SKILL_MD = "SKILL.md";

  static class Hints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
      hints.resources().registerResource(new ClassPathResource(SKILL_MD));
    }
  }

  @Mixin
  private HelpMixin helpMixin;

  @Spec
  private CommandSpec spec;

  @Override
  public Integer call() {
    var resource = new ClassPathResource(SKILL_MD);
    try {
      var content = resource.getContentAsString(StandardCharsets.UTF_8);
      var out = spec.commandLine().getOut();
      out.print(content);
      out.flush();
      return 0;
    } catch (IOException e) {
      spec.commandLine().getErr().println("Could not read %s from classpath: %s".formatted(SKILL_MD, e.getMessage()));
      return 1;
    }
  }
}
