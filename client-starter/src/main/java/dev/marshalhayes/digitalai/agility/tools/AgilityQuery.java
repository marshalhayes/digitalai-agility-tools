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
  public final String from;

  @JsonProperty("select")
  public final List<Object> select;

  @JsonProperty("where")
  public final Map<String, Object> where;

  @JsonProperty("sort")
  public final List<String> sort;

  @JsonProperty("page")
  public PageSpec page;

  private AgilityQuery(String from, List<Object> select, Map<String, Object> where,
      List<String> sort, PageSpec page) {
    this.from = from;
    this.select = select;
    this.where = where;
    this.sort = sort;
    this.page = page;
  }

  public static Builder builder(String from) {
    return new Builder(from);
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
    private final String from;
    private final Map<String, Object> where = new LinkedHashMap<>();
    private final List<String> sort = new ArrayList<>();
    private PageSpec page;

    Builder(String from) {
      this.from = from;
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

    public AgilityQuery select(Object... fields) {
      return new AgilityQuery(from, List.of(fields), where, sort, page);
    }
  }
}
