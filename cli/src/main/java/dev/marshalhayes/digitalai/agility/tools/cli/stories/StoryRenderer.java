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
import com.github.ajalt.mordant.widgets.HorizontalRule;
import com.github.ajalt.mordant.widgets.Padding;
import com.github.ajalt.mordant.widgets.Panel;
import com.github.ajalt.mordant.widgets.Text;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import kotlin.Unit;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class StoryRenderer {

  private final Terminal terminal;
  private static final FlexmarkHtmlConverter HTML_TO_MD = FlexmarkHtmlConverter.builder().build();

  StoryRenderer(Terminal terminal) {
    this.terminal = terminal;
  }

  public void render(StoryView story) {
    boolean ansiHyperLinks = terminal.getTerminalInfo().getAnsiHyperLinks();

    // Title: Panel with rounded border. Story number in the border title,
    // story name as panel body. Expands to fill terminal width.
    terminal.print(titlePanel(story), false);
    terminal.println(false);

    // Metadata: compact 3-column grid (labels dim, status color-coded).
    var metadataWidget = metadataGrid(story);
    if (metadataWidget != null) {
      terminal.print(metadataWidget, false);
      terminal.println(false);
    }

    // Description: HTML converted to Markdown, rendered by Mordant.
    // hyperlinks=false prevents Mordant's OSC-8 fallback when the terminal doesn't support it.
    terminal.println(new HorizontalRule("Description", null, TextStyles.dim.getStyle(), TextAlign.LEFT, null, true), false);
    if (story.description() != null && !story.description().isBlank()) {
      var descriptionMarkdown = convertHtml(story.description());
      terminal.print(new Markdown(descriptionMarkdown, false, ansiHyperLinks ? null : Boolean.FALSE), false);
    }

    if (story.url() != null && !story.url().isBlank()) {
      terminal.println(new HorizontalRule("", null, TextStyles.dim.getStyle(), TextAlign.CENTER, null, true), false);
      String urlDisplay = ansiHyperLinks
          ? TextStyles.dim.invoke("Open in browser  ") + TextStyles.Companion.hyperlink(story.url()).invoke(story.url())
          : TextStyles.dim.invoke("Open in browser  ") + story.url();
      terminal.println(new Text(urlDisplay, Whitespace.NORMAL, TextAlign.LEFT, OverflowWrap.NORMAL, null, null), false);
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
    addField(fields, "Priority", colorizePriority(story.priority()));
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

  private static String colorizePriority(String priority) {
    if (priority == null) return null;
    return switch (priority.toLowerCase()) {
      case "critical" -> TextColors.brightRed.invoke("● " + priority);
      case "high" -> TextColors.red.invoke("● " + priority);
      case "medium" -> TextColors.yellow.invoke("● " + priority);
      case "low", "trivial" -> TextColors.gray.invoke("● " + priority);
      default -> "● " + priority;
    };
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
   * Converts HTML to Markdown. Empty table rows (produced by some Agility editors) are
   * stripped via Jsoup before conversion to avoid blank table entries in the output.
   */
  private static String convertHtml(String html) {
    var document = Jsoup.parseBodyFragment(html);
    document.select("tr").forEach(row -> {
      boolean isEmpty = row.select("td, th").stream()
          .allMatch(cell -> cell.text().isBlank() && cell.select("img").isEmpty());
      if (isEmpty) {
        row.remove();
      }
    });
    return HTML_TO_MD.convert(document.body().html());
  }
}
