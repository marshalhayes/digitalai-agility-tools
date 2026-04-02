package dev.marshalhayes.digitalai.agility.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AgilityQueryTest {

    record ScalarRecord(String number, String name, Double estimate) {}
    record RelationRecord(@JsonProperty("_oid") String oid, String name) {}
    record ComplexRecord(String number, RelationRecord status, List<RelationRecord> owners, @JsonProperty("_oid") String oid) {}

    private static final ObjectMapper mapper = JsonMapper.builder()
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Test
    void scalarFieldsProduceStringSelectItems() throws Exception {
        var query = AgilityQuery.builder("Story", mapper).select(ScalarRecord.class);
        JsonNode root = mapper.readTree(mapper.writeValueAsString(query));
        JsonNode select = root.get("select");

        assertThat(select).isNotNull();
        assertThat(select.isArray()).isTrue();
        assertThat(select).hasSize(3);

        List<String> values = new ArrayList<>();
        select.forEach(n -> values.add(n.textValue()));
        assertThat(values).containsExactlyInAnyOrder("Number", "Name", "Estimate");
    }

    @Test
    void complexRelationProducesSubquery() throws Exception {
        var query = AgilityQuery.builder("Story", mapper).select(ComplexRecord.class);
        JsonNode root = mapper.readTree(mapper.writeValueAsString(query));
        JsonNode select = root.get("select");

        boolean hasNumber = false;
        JsonNode statusSubquery = null;
        JsonNode ownersSubquery = null;

        for (JsonNode item : select) {
            if (item.isTextual() && "Number".equals(item.textValue())) {
                hasNumber = true;
            } else if (item.isObject() && "Status".equals(item.path("from").textValue())) {
                statusSubquery = item;
            } else if (item.isObject() && "Owners".equals(item.path("from").textValue())) {
                ownersSubquery = item;
            }
        }

        assertThat(hasNumber).as("Number scalar should be in select").isTrue();

        assertThat(statusSubquery).as("Status subquery should be present").isNotNull();
        List<String> statusSelectValues = new ArrayList<>();
        statusSubquery.get("select").forEach(n -> statusSelectValues.add(n.textValue()));
        assertThat(statusSelectValues).as("Status subquery select should contain Name").contains("Name");

        assertThat(ownersSubquery).as("Owners subquery should be present").isNotNull();
        List<String> ownersSelectValues = new ArrayList<>();
        ownersSubquery.get("select").forEach(n -> ownersSelectValues.add(n.textValue()));
        assertThat(ownersSelectValues).as("Owners subquery select should contain Name").contains("Name");
    }

    @Test
    void oidFieldsExcludedFromTopLevelSelect() throws Exception {
        var query = AgilityQuery.builder("Story", mapper).select(ComplexRecord.class);
        JsonNode root = mapper.readTree(mapper.writeValueAsString(query));
        JsonNode select = root.get("select");

        for (JsonNode item : select) {
            if (item.isTextual()) {
                assertThat(item.textValue()).isNotEqualTo("_oid");
                assertThat(item.textValue()).isNotEqualTo("Oid");
            }
        }
    }

    @Test
    void oidFieldsExcludedFromSubquerySelect() throws Exception {
        var query = AgilityQuery.builder("Story", mapper).select(ComplexRecord.class);
        JsonNode root = mapper.readTree(mapper.writeValueAsString(query));
        JsonNode select = root.get("select");

        for (JsonNode item : select) {
            if (item.isObject() && item.has("select")) {
                item.get("select").forEach(subItem -> {
                    if (subItem.isTextual()) {
                        assertThat(subItem.textValue()).isNotEqualTo("_oid");
                        assertThat(subItem.textValue()).isNotEqualTo("Oid");
                    }
                });
            }
        }
    }
}
