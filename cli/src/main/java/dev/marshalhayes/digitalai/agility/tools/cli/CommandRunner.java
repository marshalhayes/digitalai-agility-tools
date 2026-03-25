package dev.marshalhayes.digitalai.agility.tools.cli;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class CommandRunner implements CommandLineRunner, ExitCodeGenerator {
  private final RootCommand rootCommand;
  private final IFactory factory;

  private int exitCode;

  public CommandRunner(RootCommand rootCommand, IFactory factory) {
    this.rootCommand = rootCommand;
    this.factory = factory;
  }

  @Override
  public void run(String... args) throws Exception {
    var commandLine = new CommandLine(rootCommand, factory);

    exitCode = commandLine.execute(args);
  }

  @Override
  public int getExitCode() {
    return exitCode;
  }
}
