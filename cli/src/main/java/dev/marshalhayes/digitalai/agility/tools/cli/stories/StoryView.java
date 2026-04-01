package dev.marshalhayes.digitalai.agility.tools.cli.stories;

/**
 * Display model for a story. All fields are plain strings; any Named/entity unwrapping
 * happens in the command before passing here.
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
    String owners) {
}
