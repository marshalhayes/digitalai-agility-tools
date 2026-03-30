package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.io.PrintWriter;

import org.springframework.stereotype.Component;

import dev.marshalhayes.digitalai.agility.tools.cli.utils.BaseAnsiHtmlRenderer;
import picocli.CommandLine.Help.Ansi;

@Component
class AnsiStoryRenderer extends BaseAnsiHtmlRenderer implements StoryRenderer {
  @Override
  public void render(StoryView story, PrintWriter writer) {
    writer.println(Ansi.AUTO.string("@|bold %s - %s|@".formatted(story.number(), story.name())));
    writer.println();

    renderHtml(story.description(), writer);
  }
}
