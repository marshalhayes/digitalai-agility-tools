package dev.marshalhayes.digitalai.agility.tools.cli.mixins;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Spec.Target;
import tools.jackson.databind.ObjectMapper;

@Component
public class JsonOutputMixin {
  private final ObjectMapper objectMapper;

  @Option(names = "--json", arity = "0..*", split = ",")
  private String[] fields;

  @Spec(Target.MIXEE)
  private CommandSpec mixee;

  public JsonOutputMixin(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public boolean isRequested() {
    return fields != null;
  }

  public Object[] fieldsOrElse(Object... defaults) {
    if (fields != null && fields.length > 0) {
      return fields;
    }

    return defaults;
  }

  public void printJson(Object value) throws Exception {
    var json = objectMapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(value);

    mixee.commandLine().getOut()
        .println(json);
  }
}
