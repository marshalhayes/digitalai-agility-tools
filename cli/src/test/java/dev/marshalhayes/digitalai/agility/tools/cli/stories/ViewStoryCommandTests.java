package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ViewStoryCommandTests {
  @Test
  void shouldRenderMarkdownWithoutAnsiMarkup() {
    var markdown = ViewStoryCommand.toMarkdown(Map.of(
        "Number", "S-1001",
        "Name", "Test Story",
        "Description", "<p>Hello <strong>world</strong></p>"));

    assertThat(markdown)
        .startsWith("**S-1001** - Test Story")
        .contains("Hello **world**")
        .doesNotContain("@|bold");
  }

  @Test
  void shouldHandleMissingDescription() {
    var markdown = ViewStoryCommand.toMarkdown(Map.of(
        "Number", "S-1001",
        "Name", "Test Story"));

    assertThat(markdown).isEqualTo("**S-1001** - Test Story");
  }
}
