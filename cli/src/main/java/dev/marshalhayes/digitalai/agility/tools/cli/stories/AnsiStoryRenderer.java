package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.github.ajalt.mordant.markdown.Markdown;
import com.github.ajalt.mordant.rendering.TextAlign;
import com.github.ajalt.mordant.terminal.Terminal;
import com.github.ajalt.mordant.widgets.HorizontalRule;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
class AnsiStoryRenderer implements StoryRenderer {

  private final Terminal terminal;
  private static final FlexmarkHtmlConverter HTML_TO_MD = FlexmarkHtmlConverter.builder().build();

  // Matches [text](scheme:...) links where the scheme is non-http (e.g. mailto:, tel:, ftp:)
  // These add no value in a terminal since the scheme is unactionable; show just the text.
  private static final Pattern NON_HTTP_LINK = Pattern.compile(
      "\\[([^\\]]+)]\\((?!https?://)([^)]+)\\)");

  // Matches [text](url) where the text IS the url (auto-links). No need to repeat the url.
  private static final Pattern AUTO_LINK = Pattern.compile(
      "\\[([^\\]]+)]\\(https?://\\1\\)");

  AnsiStoryRenderer(Terminal terminal) {
    this.terminal = terminal;
  }

  @Override
  public void render(StoryView story) {
    // Title: HorizontalRule with left-aligned text keeps the decorative rule while
    // avoiding Mordant's default center-alignment for Markdown ## headings.
    terminal.print(new HorizontalRule(
        story.number() + " \u2014 " + story.name(),
        null, null, TextAlign.LEFT, null, false), false);
    terminal.println(false);

    var md = new StringBuilder();
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

    if (!md.isEmpty()) {
      terminal.print(new Markdown(md.toString(), false, null), false);
    }
  }

  /**
   * Converts HTML description to Markdown.
   *
   * <p>Pre-processes with jsoup to remove table rows whose cells contain only whitespace or
   * {@code <br>} tags (artifact of rich-text editors). Post-processes to:
   * <ul>
   *   <li>Strip non-HTTP links (mailto:, tel:, etc.) — show display text only</li>
   *   <li>Strip auto-links where display text == URL — avoids repeating the URL</li>
   *   <li>Remove unnecessary {@code \&} Markdown escaping (Mordant quirk in table cells)</li>
   * </ul>
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

    // Strip non-HTTP schemes (mailto:, tel:, ftp:, etc.) — show display text only
    markdown = NON_HTTP_LINK.matcher(markdown).replaceAll("$1");
    // Strip auto-links where text == url (no added value repeating the url)
    markdown = AUTO_LINK.matcher(markdown).replaceAll("$1");
    // Mordant doesn't render \& in table cells — & needs no escaping in Markdown anyway
    markdown = markdown.replace("\\&", "&");

    return markdown;
  }

  private static void appendField(StringBuilder md, String label, String value) {
    if (value != null && !value.isBlank()) {
      md.append("**").append(label).append(":** ").append(value).append("  \n");
    }
  }
}
