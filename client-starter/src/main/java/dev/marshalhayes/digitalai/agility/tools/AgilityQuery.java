package dev.marshalhayes.digitalai.agility.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AgilityQuery {
  @JsonProperty("from")
  private final String from;

  @JsonProperty("select")
  private final List<Object> select;

  @JsonProperty("where")
  private final Map<String, Object> where;

  @JsonProperty("sort")
  private final List<String> sort;

  @JsonProperty("page")
  private PageSpec page;

  private AgilityQuery(String from, List<Object> select, Map<String, Object> where,
      List<String> sort, PageSpec page) {
    this.from = from;
    this.select = select;
    this.where = where;
    this.sort = sort;
    this.page = page;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Map<String, Object> subquery(String relation, String... fields) {
    return Map.of("from", relation, "select", List.of(fields));
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record PageSpec(
      @JsonProperty("start") int start,
      @JsonProperty("size") int size) {
  }

  public static class Builder {
    private String from;
    private List<Object> select = List.of();
    private final Map<String, Object> where = new LinkedHashMap<>();
    private final List<String> sort = new ArrayList<>();
    private PageSpec page;

    public Builder from(String from) {
      this.from = from;

      return this;
    }

    public Builder select(Object... fields) {
      this.select = List.of(fields);

      return this;
    }

    public Builder where(String attribute, Object value) {
      where.put(attribute, value);

      return this;
    }

    public Builder sort(String... tokens) {
      sort.addAll(Arrays.asList(tokens));

      return this;
    }

    public Builder page(int start, int size) {
      this.page = new PageSpec(start, size);
      
      return this;
    }

    public AgilityQuery build() {
      return new AgilityQuery(from, select, where, sort, page);
    }
  }
}
