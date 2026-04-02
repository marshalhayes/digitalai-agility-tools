package dev.marshalhayes.digitalai.agility.tools.cli.stories;

/**
 * Display model for a story. All relation fields are flat strings; the url is
 * the fully-qualified deep-link to the story in the Agility web UI.
 */
public record StoryView(
    String number,
    String name,
    String description,
    String status,
    String priority,
    String estimate,
    String sprint,
    String project,
    String owners,
    String url) {
}
