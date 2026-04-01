package dev.marshalhayes.digitalai.agility.tools.cli.utils;

import java.io.PrintWriter;
import java.util.List;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Renders a list of rows as a box-drawing table.
 *
 * <p>Accepts {@link AttributedString} cells so that JLine3 can report accurate visible widths
 * without needing to strip ANSI escape codes manually.
 */
public class AnsiTableRenderer {

  public static void render(List<AttributedString[]> rows, PrintWriter writer) {
    if (rows.isEmpty()) {
      return;
    }

    var columnCount = rows.stream().mapToInt(r -> r.length).max().orElse(0);
    var columnWidths = new int[columnCount];

    for (var row : rows) {
      for (var col = 0; col < row.length; col++) {
        columnWidths[col] = Math.max(columnWidths[col], row[col].length());
      }
    }

    var topBorder = buildBorder('┌', '─', '┬', '┐', columnWidths);
    var midBorder = buildBorder('├', '─', '┼', '┤', columnWidths);
    var botBorder = buildBorder('└', '─', '┴', '┘', columnWidths);

    writer.println(topBorder);

    for (var rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      var row = rows.get(rowIndex);
      var line = new AttributedStringBuilder();

      for (var col = 0; col < columnCount; col++) {
        line.append("│ ", AttributedStyle.DEFAULT);
        if (col < row.length) {
          line.append(row[col]);
          line.append(" ".repeat(columnWidths[col] - row[col].length()), AttributedStyle.DEFAULT);
        } else {
          line.append(" ".repeat(columnWidths[col]), AttributedStyle.DEFAULT);
        }
        line.append(" ", AttributedStyle.DEFAULT);
      }

      line.append("│", AttributedStyle.DEFAULT);
      writer.println(line.toAnsi());

      if (rowIndex < rows.size() - 1) {
        writer.println(midBorder);
      }
    }

    writer.println(botBorder);
  }

  private static String buildBorder(char left, char fill, char mid, char right, int[] columnWidths) {
    var sb = new StringBuilder().append(left);
    for (var col = 0; col < columnWidths.length; col++) {
      if (col > 0) sb.append(mid);
      sb.append(String.valueOf(fill).repeat(columnWidths[col] + 2));
    }
    return sb.append(right).toString();
  }
}
