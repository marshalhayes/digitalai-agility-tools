package dev.marshalhayes.digitalai.agility.tools.cli;

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;

public final class HtmlConverter {
  private static final FlexmarkHtmlConverter CONVERTER = FlexmarkHtmlConverter
      .builder(new MutableDataSet()
          .set(FlexmarkHtmlConverter.BR_AS_EXTRA_BLANK_LINES, false)
          .set(FlexmarkHtmlConverter.BR_AS_PARA_BREAKS, false)
          .set(FlexmarkHtmlConverter.MAX_TRAILING_BLANK_LINES, 1)
          .set(FlexmarkHtmlConverter.SKIP_CHAR_ESCAPE, true))
      .build();

  private HtmlConverter() {
  }

  public static String convert(String html) {
    if (html == null || html.isBlank()) {
      return "";
    }

    return CONVERTER.convert(html, -1);
  }
}
