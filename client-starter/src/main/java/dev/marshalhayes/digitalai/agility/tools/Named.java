package dev.marshalhayes.digitalai.agility.tools;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A named Agility relation — e.g. Status, Priority, Timebox, Scope, Member.
 * Agility's query.v1 returns these as {"Name": "..."} objects.
 */
public record Named(@JsonProperty("Name") String name) {
}
