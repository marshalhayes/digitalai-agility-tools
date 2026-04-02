package dev.marshalhayes.digitalai.agility.tools.cli.stories;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class StoryDeserializationTest {

    private static final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void storyFieldsDeserializeWithExplicitJsonProperty() throws Exception {
        var story = mapper.readValue(storyJson(), Story.class);

        assertThat(story.oid()).isEqualTo("Story:10612");
        assertThat(story.number()).isEqualTo("S-05747");
        assertThat(story.name()).isEqualTo("Test Story");
    }

    @Test
    void statusIsDeserializedAsFirstElementOfArray() throws Exception {
        var story = mapper.readValue(storyJson(), Story.class);

        assertThat(story.status()).isNotNull().hasSize(1);
        assertThat(story.status().get(0).name()).isEqualTo("In Progress");
    }

    @Test
    void convertValueFromJsonNodeDeserializesCorrectly() throws Exception {
        var node = mapper.readTree(storyJson());
        var story = mapper.convertValue(node, Story.class);

        assertThat(story.oid()).isEqualTo("Story:10612");
        assertThat(story.number()).isEqualTo("S-05747");
        assertThat(story.name()).isEqualTo("Test Story");
    }

    private static String storyJson() {
        return """
                {"_oid":"Story:10612","Number":"S-05747","Name":"Test Story",
                 "Status":[{"_oid":"StoryStatus:1","Name":"In Progress"}]}
                """;
    }
}
