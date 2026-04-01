package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.github.ajalt.mordant.markdown.Markdown;
import com.github.ajalt.mordant.terminal.Terminal;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
class AnsiStoryRenderer implements StoryRenderer {

  private final Terminal terminal;
  private static final FlexmarkHtmlConverter HTML_TO_MD = FlexmarkHtmlConverter.builder().build();

  AnsiStoryRenderer(Terminal terminal) {
    this.terminal = terminal;
  }

  @Override
  public void render(StoryView story) {
    var md = new StringBuilder();

    md.append("## ").append(story.number()).append(" \u2014 ").append(story.name()).append("\n\n");

    appendField(md, "Status", story.status());
    appendField(md, "Priority", story.priority());
    appendField(md, "Estimate", story.estimate());
    appendField(md, "Sprint", story.sprint());
    appendField(md, "Project", story.project());
    appendField(md, "Owners", story.owners());

    if (story.description() != null && !story.description().isBlank()) {
      md.append("\n---\n\n");
      md.append(convertHtml(story.description()));
    }

    terminal.print(new Markdown(md.toString(), false, null), true);
  }

  /**
   * Converts HTML description to Markdown.
   *
   * <p>Pre-processes with jsoup to remove table rows whose cells contain only whitespace or
   * {@code <br>} tags (artifact of rich-text editors). Post-processes to remove unnecessary
   * Markdown escaping of {@code \&} — Mordant doesn't render escaped ampersands in table cells.
   */
  private static String convertHtml(String html) {
    var doc = Jsoup.parseBodyFragment(html);
    doc.select("tr").forEach(row -> {
      boolean isEmpty = row.select("td, th").stream()
          .allMatch(cell -> cell.text().isBlank() && cell.select("img").isEmpty());
      if (isEmpty) {
        row.remove();
      }
    });
    var markdown = HTML_TO_MD.convert(doc.body().html());
    // Mordant doesn't render `\&` in table cells — & needs no escaping in Markdown anyway
    return markdown.replace("\\&", "&");
  }

  private static void appendField(StringBuilder md, String label, String value) {
    if (value != null && !value.isBlank()) {
      md.append("**").append(label).append(":** ").append(value).append("  \n");
    }
  }
}
