package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.github.ajalt.mordant.markdown.Markdown;
import com.github.ajalt.mordant.rendering.BorderType;
import com.github.ajalt.mordant.rendering.TextAlign;
import com.github.ajalt.mordant.rendering.TextColors;
import com.github.ajalt.mordant.rendering.TextStyles;
import com.github.ajalt.mordant.rendering.OverflowWrap;
import com.github.ajalt.mordant.rendering.Widget;
import com.github.ajalt.mordant.rendering.Whitespace;
import com.github.ajalt.mordant.table.TableDslKt;
import com.github.ajalt.mordant.terminal.Terminal;
import com.github.ajalt.mordant.widgets.Padding;
import com.github.ajalt.mordant.widgets.Panel;
import com.github.ajalt.mordant.widgets.Text;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import kotlin.Unit;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
class AnsiStoryRenderer implements StoryRenderer {

  private final Terminal terminal;
  private static final FlexmarkHtmlConverter HTML_TO_MD = FlexmarkHtmlConverter.builder().build();

  /** Strips all Markdown link URLs, leaving only the display text. */
  private static final Pattern ALL_LINKS = Pattern.compile("\\[([^\\]]+)]\\([^)]+\\)");

  AnsiStoryRenderer(Terminal terminal) {
    this.terminal = terminal;
  }

  @Override
  public void render(StoryView story) {
    boolean interactive = terminal.getInfo().getInteractive();
    boolean ansiHyperLinks = terminal.getInfo().getAnsiHyperLinks();

    // Title: Panel with rounded border. Story number in the border title,
    // story name as panel body. Expands to fill terminal width.
    terminal.print(titlePanel(story), false);
    terminal.println(false);

    // Metadata: compact 3-column grid (labels dim, status color-coded).
    var meta = metadataGrid(story);
    if (meta != null) {
      terminal.print(meta, false);
      terminal.println(false);
    }

    // Description: HTML converted to Markdown, rendered by Mordant.
    if (story.description() != null && !story.description().isBlank()) {
      var md = convertHtml(story.description(), interactive, ansiHyperLinks);
      // hyperlinks=false prevents Mordant's text(url) fallback when OSC-8 is unavailable.
      terminal.print(new Markdown(md, false, ansiHyperLinks ? null : Boolean.FALSE), false);
    }
  }

  private Widget titlePanel(StoryView story) {
    return new Panel(
        new Text(story.name(), Whitespace.NORMAL, TextAlign.LEFT, OverflowWrap.NORMAL, null, null),
        new Text(story.number(), Whitespace.PRE, TextAlign.LEFT, OverflowWrap.NORMAL, null, null),
        null,
        true,
        new Padding(0, 1, 0, 1),
        BorderType.Companion.getROUNDED(),
        TextAlign.LEFT,
        TextAlign.LEFT,
        TextStyles.dim.getStyle(),
        null
    );
  }

  private Widget metadataGrid(StoryView story) {
    var fields = new ArrayList<String[]>();
    addField(fields, "Status", colorizeStatus(story.status()));
    addField(fields, "Priority", story.priority());
    addField(fields, "Estimate", story.estimate());
    addField(fields, "Sprint", story.sprint());
    addField(fields, "Project", story.project());
    addField(fields, "Owners", story.owners());

    if (fields.isEmpty()) return null;

    // Split fields into rows of 3 for a compact multi-column layout.
    var rows = new ArrayList<List<String[]>>();
    for (int i = 0; i < fields.size(); i += 3) {
      rows.add(fields.subList(i, Math.min(i + 3, fields.size())));
    }

    return TableDslKt.grid(gridBuilder -> {
      for (var rowFields : rows) {
        gridBuilder.row(rowBuilder -> {
          for (var field : rowFields) {
            rowBuilder.cell(field[0] + field[1], cellBuilder -> {
              cellBuilder.setPadding(new Padding(0, 3, 0, 0));
              return Unit.INSTANCE;
            });
          }
          return Unit.INSTANCE;
        });
      }
      return Unit.INSTANCE;
    });
  }

  private static void addField(List<String[]> fields, String label, String value) {
    if (value != null && !value.isBlank()) {
      // Label styled dim, value unstyled (or pre-colored via colorizeStatus).
      fields.add(new String[]{TextStyles.dim.invoke(label + "  "), value});
    }
  }

  private static String colorizeStatus(String status) {
    if (status == null) return null;
    return switch (status.toLowerCase()) {
      case "done", "accepted", "closed", "completed" -> TextColors.brightGreen.invoke(status);
      case "in progress", "active", "in-progress" -> TextColors.brightYellow.invoke(status);
      case "future", "backlog", "unassigned" -> TextColors.gray.invoke(status);
      default -> status;
    };
  }

  /**
   * Converts HTML to Markdown, removing empty table rows and handling links.
   *
   * <ul>
   *   <li>TTY + OSC-8: keep all links — Mordant renders them as clickable hyperlinks</li>
   *   <li>Otherwise: strip link URLs to display text only (avoids {@code text(url)} clutter)</li>
   *   <li>Both: remove {@code \&} escaping (Mordant quirk in table cells)</li>
   * </ul>
   */
  private static String convertHtml(String html, boolean interactive, boolean ansiHyperLinks) {
    var doc = Jsoup.parseBodyFragment(html);
    doc.select("tr").forEach(row -> {
      boolean isEmpty = row.select("td, th").stream()
          .allMatch(cell -> cell.text().isBlank() && cell.select("img").isEmpty());
      if (isEmpty) {
        row.remove();
      }
    });

    var markdown = HTML_TO_MD.convert(doc.body().html());

    if (!interactive || !ansiHyperLinks) {
      // Strip link URLs when OSC-8 unavailable to avoid text(url) noise
      markdown = ALL_LINKS.matcher(markdown).replaceAll("$1");
    }
    // Mordant doesn't render \& in table cells — & needs no escaping in Markdown
    markdown = markdown.replace("\\&", "&");
    // Collapse 3+ consecutive newlines to 2 (single blank line) to reduce noise
    markdown = markdown.replaceAll("\n{3,}", "\n\n");

    return markdown;
  }
}
