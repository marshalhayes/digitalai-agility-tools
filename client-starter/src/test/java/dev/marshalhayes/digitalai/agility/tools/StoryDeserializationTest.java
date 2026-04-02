package dev.marshalhayes.digitalai.agility.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class StoryDeserializationTest {
  record TestRelation(@JsonProperty("_oid") String oid, String name) {
  }

  record TestMember(@JsonProperty("_oid") String oid, String name) {
  }

  record TestAsset(
      String number,
      String name,
      TestRelation status,
      List<TestMember> owners,
      @JsonProperty("_oid") String oid) {
  }

  private static final ObjectMapper mapper = JsonMapper.builder()
      .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
      .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  private static final String STORY_JSON = """
      {"_oid": "Story:1045", "Number": "S-1234", "Name": "My Story", "Status": [{"_oid": "StoryStatus:134", "Name": "In Progress"}]}
      """;

  @Test
  void singleElementArrayIsUnwrappedToTypedRecord() throws Exception {
    TestAsset asset = mapper.readValue(STORY_JSON, TestAsset.class);

    assertThat(asset.status().oid()).isEqualTo("StoryStatus:134");
    assertThat(asset.status().name()).isEqualTo("In Progress");
  }

  @Test
  void pascalCaseKeysDeserializeToLowercaseFields() throws Exception {
    TestAsset asset = mapper.readValue(STORY_JSON, TestAsset.class);

    assertThat(asset.number()).isEqualTo("S-1234");
    assertThat(asset.name()).isEqualTo("My Story");
  }

  @Test
  void oidFieldCapturedFromUnderscoreKey() throws Exception {
    TestAsset asset = mapper.readValue(STORY_JSON, TestAsset.class);

    assertThat(asset.oid()).isEqualTo("Story:1045");
  }

  @Test
  void multiValueOwnersDeserializesToList() throws Exception {
    String json = """
        {"_oid": "Story:1", "Number": "S-1", "Name": "T", "Owners": [{"_oid": "Member:1", "Name": "Alice"}, {"_oid": "Member:2", "Name": "Bob"}]}
        """;
    TestAsset asset = mapper.readValue(json, TestAsset.class);

    assertThat(asset.owners()).hasSize(2);
    assertThat(asset.owners().get(0).name()).isEqualTo("Alice");
  }

  @Test
  void unknownFieldsAreIgnored() throws Exception {
    String json = """
        {"_oid": "Story:1", "Number": "S-1", "Name": "Test", "CustomField123": "value", "Status": [{"_oid": "StoryStatus:1", "Name": "Open"}]}
        """;
    assertThatNoException().isThrownBy(() -> {
      TestAsset asset = mapper.readValue(json, TestAsset.class);
      assertThat(asset.number()).isEqualTo("S-1");
      assertThat(asset.name()).isEqualTo("Test");
    });
  }

  @Test
  void missingRelationFieldIsNull() throws Exception {
    String json = """
        {"_oid": "Story:1", "Number": "S-1", "Name": "Test"}
        """;
    TestAsset asset = mapper.readValue(json, TestAsset.class);

    assertThat(asset.status()).isNull();
  }
}
