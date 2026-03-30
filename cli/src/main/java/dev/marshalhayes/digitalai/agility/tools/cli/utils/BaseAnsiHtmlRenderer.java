package dev.marshalhayes.digitalai.agility.tools.cli.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import picocli.CommandLine.Help.Ansi;

public abstract class BaseAnsiHtmlRenderer {
  protected void renderHtml(String html, PrintWriter writer) {
    if (html == null || html.isBlank()) {
      return;
    }

    var body = Jsoup.parseBodyFragment(html)
        .body();

    var inner = new StringWriter();
    var innerWriter = new PrintWriter(inner);

    renderChildren(body, innerWriter);

    var rendered = inner.toString()
        .replaceAll("\\n{3,}", "\n\n")
        .strip();

    writer.print(Ansi.AUTO.string(rendered));
  }

  private void renderChildren(Element parent, PrintWriter writer) {
    for (var child : parent.childNodes()) {
      renderNode(child, writer);
    }
  }

  private void renderNode(Node node, PrintWriter writer) {
    if (node instanceof TextNode textNode) {
      writer.print(textNode.getWholeText());
    } else if (node instanceof Comment) {
      // skip comments
    } else if (node instanceof Element el) {
      renderElement(el, writer);
    }
  }

  private void renderElement(Element el, PrintWriter writer) {
    var tag = el.normalName();

    switch (tag) {
      case "b", "strong" -> styled(writer, "bold", el);

      case "i", "em" -> styled(writer, "italic", el);

      case "u" -> {
        writer.print("\033[4m");
        renderChildren(el, writer);
        writer.print("\033[24m");
      }

      case "h1", "h2", "h3", "h4", "h5", "h6" -> {
        var inner = new StringWriter();
        var innerWriter = new PrintWriter(inner);

        styled(innerWriter, "bold", el);

        writer.print(inner.toString().stripTrailing());
        writer.println();
      }

      case "p" -> {
        var inner = new StringWriter();
        var innerWriter = new PrintWriter(inner);

        renderChildren(el, innerWriter);

        writer.print(inner.toString().stripTrailing());
        writer.println();
      }

      case "br" -> writer.println();

      case "ul" -> {
        for (var child : el.children()) {
          writer.print("• ");

          renderChildren(child, writer);

          writer.println();
        }
      }

      case "ol" -> {
        var itemNumber = 1;

        for (var child : el.children()) {
          writer.print("%d. ".formatted(itemNumber++));

          renderChildren(child, writer);

          writer.println();
        }
      }

      case "a" -> {
        var href = el.attr("href");

        if (!href.isBlank()) {
          writer.print("\033]8;;");
          writer.print(href);
          writer.print("\007");
          renderChildren(el, writer);
          writer.print("\033]8;;\007");
        } else {
          renderChildren(el, writer);
        }
      }

      case "img" -> {
        var src = el.attr("src");
        var alt = el.attr("alt");

        writer.print("\uD83D\uDCF8 ");

        if (!src.isBlank()) {
          writer.print("\033]8;;");
          writer.print(src);
          writer.print("\007");
          writer.print(alt.isBlank() ? src : alt);
          writer.print("\033]8;;\007");
        } else if (!alt.isBlank()) {
          writer.print(alt);
        }
      }

      case "blockquote" -> {
        var block = new StringWriter();
        var blockWriter = new PrintWriter(block);

        renderChildren(el, blockWriter);

        for (var line : block.toString().split("\n", -1)) {
          if (!line.isBlank()) {
            writer.print("> ");
            writer.println(line);
          }
        }
      }

      case "hr" -> writer.println("\n---");

      case "table" -> {
        var rows = new ArrayList<String[]>();

        for (var row : el.select("tr")) {
          var cells = new ArrayList<String>();

          for (var cell : row.children()) {
            var cellTag = cell.normalName();

            if (cellTag.equals("th") || cellTag.equals("td")) {
              var text = extractCellText(cell);

              if (cellTag.equals("th")) {
                text = "@|bold " + text + "|@";
              }

              cells.add(text);
            }
          }

          if (!cells.isEmpty()) {
            rows.add(cells.toArray(String[]::new));
          }
        }

        writer.println();
        AnsiTableRenderer.render(rows, writer);
      }

      case "div" -> renderChildren(el, writer);

      case "span" -> {
        var style = el.attr("style");

        if (style.contains("text-decoration: line-through")) {
          writer.print("\033[9m");
          renderChildren(el, writer);
          writer.print("\033[29m");
        } else if (style.contains("text-decoration: underline")) {
          writer.print("\033[4m");
          renderChildren(el, writer);
          writer.print("\033[24m");
        } else {
          renderChildren(el, writer);
        }
      }

      default -> renderChildren(el, writer);
    }
  }

  private String extractCellText(Element cell) {
    var stringWriter = new StringWriter();
    var cellWriter = new PrintWriter(stringWriter);

    renderChildren(cell, cellWriter);

    return stringWriter.toString()
        .strip();
  }

  private void styled(PrintWriter writer, String style, Element el) {
    writer.print("@|");
    writer.print(style);
    writer.print(" ");

    renderChildren(el, writer);

    writer.print("|@");
  }
}
