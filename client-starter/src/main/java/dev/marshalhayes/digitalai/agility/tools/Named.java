package dev.marshalhayes.digitalai.agility.tools;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A named Agility relation — e.g. Status, Priority, Timebox, Scope, Member.
 * Agility's query.v1 returns these as {"_oid": "...", "Name": "..."} objects.
 * The _oid and any other fields are ignored; only Name is captured.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Named(@JsonProperty("Name") String name) {
}
