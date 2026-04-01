package dev.marshalhayes.digitalai.agility.tools;

/**
 * A named Agility relation — e.g. Status, Priority, Timebox, Scope, Member.
 * Agility's query.v1 returns these as {"Name": "..."} objects.
 */
public record Named(String name) {
}
