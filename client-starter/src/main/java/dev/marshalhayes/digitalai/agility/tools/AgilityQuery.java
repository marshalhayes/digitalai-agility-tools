package dev.marshalhayes.digitalai.agility.tools;

import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.introspect.BeanPropertyDefinition;
import tools.jackson.databind.introspect.ClassIntrospector;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AgilityQuery<T> {
  private static ObjectMapper objectMapper = new ObjectMapper();

  public static void configure(ObjectMapper mapper) {
    objectMapper = mapper;
  }

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

  private final transient Class<T> type;

  private AgilityQuery(String from, List<Object> select, Map<String, Object> where,
      List<String> sort, PageSpec page, Class<T> type) {
    this.from = from;
    this.select = select;
    this.where = where;
    this.sort = sort;
    this.page = page;
    this.type = type;
  }

  public static Builder from(String assetType) {
    return new Builder(assetType);
  }

  public T map(JsonNode json) {
    return objectMapper.convertValue(json, type);
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

    private Builder(String from) {
      this.from = from;
    }

    public Builder where(String attribute, Object value) {
      where.put(attribute, value);

      return this;
    }

    public Builder sort(String... tokens) {
      for (var token : tokens) {
        sort.add(token);
      }

      return this;
    }

    public Builder page(int start, int size) {
      this.page = new PageSpec(start, size);

      return this;
    }

    public <T> AgilityQuery<T> select(Class<T> type) {
      var introspector = objectMapper.serializationConfig()
          .classIntrospectorInstance();

      var selectItems = buildSelectItems(type, introspector);

      return new AgilityQuery<>(from, selectItems, where, sort, page, type);
    }
  }

  private static List<Object> buildSelectItems(Class<?> type, ClassIntrospector introspector) {
    var beanDesc = introspect(type, introspector);
    var items = new ArrayList<Object>();

    for (var prop : beanDesc.findProperties()) {
      if (!prop.couldDeserialize()) {
        continue;
      }

      items.add(toField(prop, introspector));
    }

    return items;
  }

  private static Object toField(BeanPropertyDefinition prop, ClassIntrospector introspector) {
    var name = StringUtils.capitalize(prop.getName());
    var propType = prop.getPrimaryType();

    if (isComplexType(propType)) {
      return subquery(name, propType.getRawClass(), introspector);
    }

    if (propType.isContainerType() && isComplexType(propType.getContentType())) {
      return subquery(name, propType.getContentType().getRawClass(), introspector);
    }

    return name;
  }

  private static BeanDescription introspect(Class<?> type, ClassIntrospector introspector) {
    var javaType = objectMapper.constructType(type);

    var annotatedClass = introspector
        .introspectClassAnnotations(javaType);

    return introspector.introspectForCreation(javaType, annotatedClass);
  }

  private static Object subquery(String relationName, Class<?> type,
      ClassIntrospector introspector) {
    var beanDesc = introspect(type, introspector);

    var fields = beanDesc.findProperties()
        .stream()
        .filter(BeanPropertyDefinition::couldDeserialize)
        .map(p -> toField(p, introspector))
        .toList();

    return Map.of("from", relationName, "select", fields);
  }

  private static boolean isComplexType(JavaType type) {
    return type != null
        && !type.isPrimitive()
        && !type.isEnumType()
        && !isScalarType(type.getRawClass())
        && !type.isContainerType();
  }

  private static boolean isScalarType(Class<?> rawClass) {
    return rawClass.equals(String.class)
        || CharSequence.class.isAssignableFrom(rawClass)
        || Number.class.isAssignableFrom(rawClass)
        || rawClass.equals(Boolean.class)
        || rawClass.equals(Character.class)
        || Date.class.isAssignableFrom(rawClass)
        || UUID.class.isAssignableFrom(rawClass)
        || TemporalAccessor.class.isAssignableFrom(rawClass);
  }
}
