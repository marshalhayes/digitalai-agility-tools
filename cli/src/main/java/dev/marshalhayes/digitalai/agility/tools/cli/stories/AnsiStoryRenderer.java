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

  /**
   * Strips ALL Markdown link URLs, leaving only the display text.
   * Used in non-TTY mode where OSC-8 is unavailable and {@code text(url)} is noisy.
   */
  private static final Pattern ALL_LINKS = Pattern.compile("\\[([^\\]]+)]\\([^)]+\\)");

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

    boolean interactive = terminal.getInfo().getInteractive();

    var md = new StringBuilder();
    appendField(md, "Status", story.status());
    appendField(md, "Priority", story.priority());
    appendField(md, "Estimate", story.estimate());
    appendField(md, "Sprint", story.sprint());
    appendField(md, "Project", story.project());
    appendField(md, "Owners", story.owners());

    if (story.description() != null && !story.description().isBlank()) {
      md.append("\n---\n\n");
      md.append(convertHtml(story.description(), interactive));
    }

    if (!md.isEmpty()) {
      // hyperlinks=null: Mordant auto-detects OSC-8 support from the terminal.
      // In TTY mode links render as clickable OSC-8 sequences (text-only visible).
      // In non-TTY mode we've already stripped link URLs, so this is moot.
      terminal.print(new Markdown(md.toString(), false, null), false);
    }
  }

  /**
   * Converts HTML description to Markdown.
   *
   * <p>Pre-processes with jsoup to remove table rows whose cells contain only whitespace or
   * {@code <br>} tags (artifact of rich-text editors). Post-processes based on rendering mode:
   * <ul>
   *   <li>TTY: keep all links — Mordant renders them as OSC-8 hyperlinks (clickable, no visible URL)</li>
   *   <li>non-TTY: strip all link URLs — display text only, no {@code text(url)} noise in piped output</li>
   *   <li>Both: remove unnecessary {@code \&} Markdown escaping (Mordant quirk in table cells)</li>
   * </ul>
   */
  private static String convertHtml(String html, boolean interactive) {
    var doc = Jsoup.parseBodyFragment(html);
    doc.select("tr").forEach(row -> {
      boolean isEmpty = row.select("td, th").stream()
          .allMatch(cell -> cell.text().isBlank() && cell.select("img").isEmpty());
      if (isEmpty) {
        row.remove();
      }
    });

    var markdown = HTML_TO_MD.convert(doc.body().html());

    if (!interactive) {
      // Non-TTY: strip all link URLs — show display text only, no text(url) clutter
      markdown = ALL_LINKS.matcher(markdown).replaceAll("$1");
    }
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
