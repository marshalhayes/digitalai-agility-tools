package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Domain projection for a Story as returned by the Agility query API.
 *
 * <p>All fields carry explicit {@code @JsonProperty} annotations so this record
 * is self-describing and works with a plain Jackson ObjectMapper.  Relation
 * fields are typed as {@code List<X>} because the Agility API always returns
 * relations as arrays; callers extract the first element when a single value
 * is expected.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Story(
    @JsonProperty("Number") String number,
    @JsonProperty("Name") String name,
    @JsonProperty("Description") String description,
    @JsonProperty("Status") List<Status> status,
    @JsonProperty("Priority") List<Priority> priority,
    @JsonProperty("Estimate") Double estimate,
    @JsonProperty("Timebox") List<Timebox> timebox,
    @JsonProperty("Scope") List<Scope> scope,
    @JsonProperty("Owners") List<Member> owners,
    @JsonProperty("_oid") String oid) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Status(@JsonProperty("_oid") String oid, @JsonProperty("Name") String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Priority(@JsonProperty("_oid") String oid, @JsonProperty("Name") String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Timebox(@JsonProperty("_oid") String oid, @JsonProperty("Name") String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Scope(@JsonProperty("_oid") String oid, @JsonProperty("Name") String name) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Member(@JsonProperty("_oid") String oid, @JsonProperty("Name") String name) {}
}
