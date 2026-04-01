package dev.marshalhayes.digitalai.agility.tools.cli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Converts HTML to terminal-appropriate strings.
 *
 * <p>Uses JLine3 {@link AttributedStringBuilder} for inline styling (bold, italic, underline,
 * etc.) so there is no mixing of raw ANSI codes and picocli markup. The two public methods
 * produce either ANSI-escaped output (for TTY) or plain text (for pipes/JSON).
 */
public final class HtmlRenderer {

  private HtmlRenderer() {}

  public static String toAnsi(String html) {
    return render(html, true);
  }

  public static String toPlainText(String html) {
    return render(html, false);
  }

  private static String render(String html, boolean ansi) {
    if (html == null || html.isBlank()) {
      return "";
    }

    var body = Jsoup.parseBodyFragment(html).body();
    var asb = new AttributedStringBuilder();
    appendChildren(body, asb, AttributedStyle.DEFAULT, ansi);

    var result = ansi ? asb.toAnsi() : asb.toString();
    return result.replaceAll("\n{3,}", "\n\n").strip();
  }

  private static void appendChildren(Element parent, AttributedStringBuilder asb,
      AttributedStyle style, boolean ansi) {
    for (var child : parent.childNodes()) {
      appendNode(child, asb, style, ansi);
    }
  }

  private static void appendNode(Node node, AttributedStringBuilder asb,
      AttributedStyle style, boolean ansi) {
    if (node instanceof TextNode t) {
      asb.append(t.getWholeText(), style);
    } else if (node instanceof Element el) {
      appendElement(el, asb, style, ansi);
    }
    // comments are silently ignored
  }

  private static void appendElement(Element el, AttributedStringBuilder asb,
      AttributedStyle style, boolean ansi) {
    switch (el.normalName()) {
      case "b", "strong" -> appendChildren(el, asb, style.bold(), ansi);
      case "i", "em" -> appendChildren(el, asb, style.italic(), ansi);
      case "u" -> appendChildren(el, asb, style.underline(), ansi);
      case "s", "del", "strike" -> appendChildren(el, asb, style.crossedOut(), ansi);
      case "code" -> appendChildren(el, asb, style.foreground(AttributedStyle.CYAN), ansi);

      case "span" -> {
        var css = el.attr("style");
        var s = style;
        if (css.contains("text-decoration: line-through")) s = s.crossedOut();
        if (css.contains("text-decoration: underline")) s = s.underline();
        appendChildren(el, asb, s, ansi);
      }

      case "h1", "h2", "h3", "h4", "h5", "h6" -> {
        appendChildren(el, asb, style.bold(), ansi);
        asb.append("\n", style);
      }

      case "p" -> {
        appendChildren(el, asb, style, ansi);
        asb.append("\n", style);
      }

      case "br" -> asb.append("\n", style);

      case "ul" -> {
        for (var li : el.children()) {
          asb.append("• ", style);
          appendChildren(li, asb, style, ansi);
          asb.append("\n", style);
        }
      }

      case "ol" -> {
        var i = 1;
        for (var li : el.children()) {
          asb.append(i++ + ". ", style);
          appendChildren(li, asb, style, ansi);
          asb.append("\n", style);
        }
      }

      case "a" -> {
        var href = el.attr("href");
        if (ansi && !href.isBlank()) {
          // OSC 8 hyperlink — JLine3 doesn't natively model these, emit as raw sequences
          asb.append("\033]8;;" + href + "\007", style);
          appendChildren(el, asb, style.underline(), ansi);
          asb.append("\033]8;;\007", style);
        } else {
          appendChildren(el, asb, style, ansi);
        }
      }

      case "img" -> {
        var src = el.attr("src");
        var alt = el.attr("alt");
        asb.append("📸 ", style);
        if (ansi && !src.isBlank()) {
          asb.append("\033]8;;" + src + "\007", style);
          asb.append(alt.isBlank() ? src : alt, style);
          asb.append("\033]8;;\007", style);
        } else if (!alt.isBlank()) {
          asb.append(alt, style);
        }
      }

      case "blockquote" -> {
        // Collect inner content then prefix each non-empty line with "> "
        var inner = new AttributedStringBuilder();
        appendChildren(el, inner, style, ansi);
        var text = ansi ? inner.toAnsi() : inner.toString();
        for (var line : text.split("\n", -1)) {
          if (!line.isBlank()) {
            asb.append("> " + line + "\n", style);
          }
        }
      }

      case "hr" -> asb.append("\n---\n", style);

      case "table" -> {
        var rows = buildTableRows(el, style, ansi);
        if (!rows.isEmpty()) {
          var sw = new StringWriter();
          AnsiTableRenderer.render(rows, new PrintWriter(sw));
          asb.append("\n" + sw, style);
        }
      }

      default -> appendChildren(el, asb, style, ansi);
    }
  }

  private static List<String[]> buildTableRows(Element table, AttributedStyle style, boolean ansi) {
    var rows = new ArrayList<String[]>();
    for (var row : table.select("tr")) {
      var cells = new ArrayList<String>();
      for (var cell : row.children()) {
        var cellTag = cell.normalName();
        if (cellTag.equals("th") || cellTag.equals("td")) {
          var cellStyle = cellTag.equals("th") ? style.bold() : style;
          var cellAsb = new AttributedStringBuilder();
          appendChildren(cell, cellAsb, cellStyle, ansi);
          cells.add(ansi ? cellAsb.toAnsi() : cellAsb.toString());
        }
      }
      if (!cells.isEmpty()) {
        rows.add(cells.toArray(String[]::new));
      }
    }
    return rows;
  }
}
