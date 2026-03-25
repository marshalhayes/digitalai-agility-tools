package dev.marshalhayes.digitalai.agility.tools.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    var context = new SpringApplicationBuilder(App.class)
        .logStartupInfo(false)
        .bannerMode(Mode.OFF)
        .run(args);

    var exitCode = SpringApplication.exit(context);

    System.exit(exitCode);
  }
}
