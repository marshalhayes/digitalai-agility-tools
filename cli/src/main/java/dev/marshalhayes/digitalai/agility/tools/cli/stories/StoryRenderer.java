package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import java.io.PrintWriter;

public interface StoryRenderer {
  void render(StoryView story, PrintWriter writer);
}
