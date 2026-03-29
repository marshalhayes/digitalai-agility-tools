package dev.marshalhayes.digitalai.agility.tools.cli;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Help.Ansi;

@Component
public class HtmlToAnsiConverter {
  public String convert(String html) {
    if (html == null || html.isBlank()) {
      return "";
    }

    var body = Jsoup.parseBodyFragment(html)
        .body();

    var sb = new StringBuilder();

    renderChildren(body, sb);

    return sb.toString()
        .replaceAll("\\n{3,}", "\n\n")
        .strip();
  }

  private void renderChildren(Element parent, StringBuilder sb) {
    for (var child : parent.childNodes()) {
      renderNode(child, sb);
    }
  }

  private void renderNode(Node node, StringBuilder sb) {
    if (node instanceof TextNode textNode) {
      sb.append(textNode.getWholeText());
    } else if (node instanceof Comment) {
      // skip comments
    } else if (node instanceof Element el) {
      renderElement(el, sb);
    }
  }

  private void renderElement(Element el, StringBuilder sb) {
    var tag = el.normalName();

    switch (tag) {
      case "b", "strong" -> styled(sb, "bold", el);

      case "i", "em" -> styled(sb, "italic", el);

      case "code" -> styled(sb, "cyan", el);

      case "pre" -> {
        styled(sb, "cyan", el);

        sb.append("\n");
      }

      case "h1", "h2", "h3", "h4", "h5", "h6" -> {
        styled(sb, "bold", el);

        sb.append("\n");
      }

      case "p" -> {
        renderChildren(el, sb);

        sb.append("\n");
      }

      case "br" -> sb.append("\n");

      case "ul" -> {
        for (var child : el.children()) {
          if (child.normalName().equals("li")) {
            sb.append("• ");

            renderChildren(child, sb);

            sb.append("\n");
          }
        }
      }

      case "ol" -> {
        var i = 1;

        for (var child : el.children()) {
          if (child.normalName().equals("li")) {
            sb.append("%d. ".formatted(i++));

            renderChildren(child, sb);

            sb.append("\n");
          }
        }
      }

      case "a" -> {
        var href = el.attr("href");
        var link = new StringBuilder();

        renderChildren(el, link);

        var text = link.toString().strip();

        if (!href.isBlank()) {
          sb.append("\033]8;;").append(href).append("\007");
          sb.append(text);
          sb.append("\033]8;;\007");
        } else {
          sb.append(text);
        }
      }

      case "img" -> {
        var src = el.attr("src");
        var alt = el.attr("alt");
        var label = alt.isBlank() ? "Image" : alt;

        if (!src.isBlank()) {
          sb.append("\033]8;;").append(src).append("\007");
          sb.append("[Image: ").append(label).append("]");
          sb.append("\033]8;;\007");
        } else {
          sb.append("[Image: ").append(label).append("]");
        }
      }

      case "blockquote" -> {
        for (var child : el.childNodes()) {
          var block = new StringBuilder();

          renderNode(child, block);

          var lines = block.toString()
              .split("\n", -1);

          for (var line : lines) {
            if (!line.isBlank()) {
              sb.append("> ").append(line).append("\n");
            }
          }
        }
      }

      case "hr" -> sb.append("\n---\n");

      case "table" -> {
        sb.append("\n\n");
        renderTable(el, sb);
      }

      case "div", "span" -> renderChildren(el, sb);

      default -> renderChildren(el, sb);
    }
  }

  private void renderTable(Element table, StringBuilder sb) {
    var rows = new ArrayList<String[]>();

    for (var row : table.select("tr")) {
      var cells = new ArrayList<String>();

      for (var cell : row.children()) {
        var tagName = cell.normalName();

        if (tagName.equals("th") || tagName.equals("td")) {
          var text = extractCellText(cell);

          if (tagName.equals("th")) {
            text = "@|bold " + text + "|@";
          }

          cells.add(text);
        }
      }

      if (!cells.isEmpty()) {
        rows.add(cells.toArray(String[]::new));
      }
    }

    if (rows.isEmpty()) {
      return;
    }

    var colCount = rows.stream()
        .mapToInt(r -> r.length)
        .max()
        .orElse(0);

    var colWidths = new int[colCount];

    for (var row : rows) {
      for (var i = 0; i < row.length; i++) {
        var plain = Ansi.OFF.text(row[i]).toString();
        colWidths[i] = Math.max(colWidths[i], plain.length());
      }
    }

    var topBorder = buildBorder('┌', '─', '┬', '┐', colWidths);
    var midBorder = buildBorder('├', '─', '┼', '┤', colWidths);
    var botBorder = buildBorder('└', '─', '┴', '┘', colWidths);

    sb.append(topBorder).append("\n");

    for (var r = 0; r < rows.size(); r++) {
      var row = rows.get(r);

      for (var c = 0; c < colCount; c++) {
        sb.append("│ ");

        if (c < row.length) {
          var cell = row[c];
          sb.append(cell);

          var padding = colWidths[c] - Ansi.OFF.text(cell).toString().length();

          for (var p = 0; p < padding; p++) {
            sb.append(' ');
          }
        } else {
          sb.append(" ".repeat(colWidths[c]));
        }

        sb.append(' ');
      }

      sb.append("│\n");

      if (r < rows.size() - 1) {
        sb.append(midBorder).append("\n");
      }
    }

    sb.append(botBorder).append("\n");
  }

  private String buildBorder(char left, char fill, char mid, char right, int[] colWidths) {
    var sb = new StringBuilder();
    sb.append(left);

    for (var i = 0; i < colWidths.length; i++) {
      if (i > 0) {
        sb.append(mid);
      }

      for (var j = 0; j < colWidths[i] + 2; j++) {
        sb.append(fill);
      }
    }

    sb.append(right);
    return sb.toString();
  }

  private String extractCellText(Element cell) {
    var sb = new StringBuilder();
    renderChildren(cell, sb);
    return sb.toString().strip();
  }

  private void styled(StringBuilder sb, String style, Element el) {
    sb.append("@|").append(style).append(" ");

    renderChildren(el, sb);

    sb.append("|@");
  }
}
