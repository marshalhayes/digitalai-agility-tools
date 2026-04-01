package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.io.PrintWriter;

import dev.marshalhayes.digitalai.agility.tools.cli.utils.HtmlRenderer;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.stereotype.Component;

@Component
class AnsiStoryRenderer implements StoryRenderer {

  private static final boolean IS_TTY = System.console() != null;

  @Override
  public void render(StoryView story, PrintWriter writer) {
    var asb = new AttributedStringBuilder();

    // Title line: bold "S-12345 — Story Name"
    asb.append(story.number() + " — " + story.name(), AttributedStyle.DEFAULT.bold());
    asb.append("\n\n", AttributedStyle.DEFAULT);

    // Metadata fields
    appendField(asb, "Status", story.status());
    appendField(asb, "Priority", story.priority());
    appendField(asb, "Estimate", story.estimate());
    appendField(asb, "Sprint", story.sprint());
    appendField(asb, "Project", story.project());
    appendField(asb, "Owners", story.owners());

    writer.println(IS_TTY ? asb.toAnsi() : asb.toString());

    if (story.description() != null && !story.description().isBlank()) {
      writer.println();
      writer.println(IS_TTY
          ? HtmlRenderer.toAnsi(story.description())
          : HtmlRenderer.toPlainText(story.description()));
    }
  }

  private static void appendField(AttributedStringBuilder asb, String label, String value) {
    if (value == null || value.isBlank()) return;
    asb.append(label + ": ", AttributedStyle.DEFAULT.bold());
    asb.append(value + "\n", AttributedStyle.DEFAULT);
  }
}
