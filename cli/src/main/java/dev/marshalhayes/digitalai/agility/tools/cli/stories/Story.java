package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Domain projection for a Story as returned by the Agility query API.
 * Relation fields use concrete inner record types so the renderer can access
 * both display names and OIDs (for deep-link URL construction).
 *
 * <p>Field names are lowercase/camelCase; Jackson's ACCEPT_CASE_INSENSITIVE_PROPERTIES
 * maps Agility's PascalCase keys (e.g. "Number", "Status") during deserialization.
 * UNWRAP_SINGLE_VALUE_ARRAYS unwraps single-element relation arrays into the typed record.
 * FAIL_ON_UNKNOWN_PROPERTIES is disabled globally on the agilityObjectMapper.
 *
 * <p>The {@code _oid} field uses an explicit {@code @JsonProperty} because the underscore
 * prefix is not handled by case-insensitive matching alone.
 */
public record Story(
    String number,
    String name,
    String description,
    Status status,
    Priority priority,
    Double estimate,
    Timebox timebox,
    Scope scope,
    List<Member> owners,
    @JsonProperty("_oid") String oid
) {

    public record Status(@JsonProperty("_oid") String oid, String name) {}

    public record Priority(@JsonProperty("_oid") String oid, String name) {}

    public record Timebox(@JsonProperty("_oid") String oid, String name) {}

    public record Scope(@JsonProperty("_oid") String oid, String name) {}

    public record Member(@JsonProperty("_oid") String oid, String name) {}
}
