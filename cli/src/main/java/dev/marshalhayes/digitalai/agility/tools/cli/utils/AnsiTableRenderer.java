package dev.marshalhayes.digitalai.agility.tools.cli.utils;

import java.io.PrintWriter;
import java.util.List;

public class AnsiTableRenderer {
  // Matches SGR sequences (\033[...m) and OSC 8 hyperlinks (\033]8;;href\007...\033]8;;\007)
  private static final java.util.regex.Pattern ANSI_STRIP =
      java.util.regex.Pattern.compile("\033\\[[0-9;]*[mA-Z]|\033]8;;[^\007]*\007");

  private static String visibleWidth(String cell) {
    if (cell == null) return "";
    return ANSI_STRIP.matcher(cell).replaceAll("");
  }

  public static void render(List<String[]> rows, PrintWriter writer) {
    if (rows.isEmpty()) {
      return;
    }

    var columnCount = rows.stream()
        .mapToInt(r -> r.length)
        .max()
        .orElse(0);

    var columnWidths = new int[columnCount];

    for (var row : rows) {
      for (var columnIndex = 0; columnIndex < row.length; columnIndex++) {
        var text = visibleWidth(row[columnIndex]);

        columnWidths[columnIndex] = Math.max(columnWidths[columnIndex], text.length());
      }
    }

    var topBorder = buildBorder('┌', '─', '┬', '┐', columnWidths);
    var midBorder = buildBorder('├', '─', '┼', '┤', columnWidths);
    var botBorder = buildBorder('└', '─', '┴', '┘', columnWidths);

    writer.println(topBorder);

    for (var rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      var row = rows.get(rowIndex);

      for (var columnIndex = 0; columnIndex < columnCount; columnIndex++) {
        writer.print("│ ");

        if (columnIndex < row.length) {
          var cell = row[columnIndex];

          writer.print(cell);

          var padding = columnWidths[columnIndex] - visibleWidth(cell).length();

          for (var paddingIndex = 0; paddingIndex < padding; paddingIndex++) {
            writer.print(' ');
          }
        } else {
          writer.print(" ".repeat(columnWidths[columnIndex]));
        }

        writer.print(' ');
      }

      writer.println("│");

      if (rowIndex < rows.size() - 1) {
        writer.println(midBorder);
      }
    }

    writer.println(botBorder);
  }

  private static String buildBorder(char left, char fill, char mid, char right, int[] columnWidths) {
    var stringBuilder = new StringBuilder();

    stringBuilder.append(left);

    for (var columnIndex = 0; columnIndex < columnWidths.length; columnIndex++) {
      if (columnIndex > 0) {
        stringBuilder.append(mid);
      }

      for (var fillIndex = 0; fillIndex < columnWidths[columnIndex] + 2; fillIndex++) {
        stringBuilder.append(fill);
      }
    }

    stringBuilder.append(right);

    return stringBuilder.toString();
  }
}
