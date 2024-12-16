package io.resys.hdes.client.test;


import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.PgProfile;
import io.resys.hdes.client.test.config.PgTestTemplate;
import io.resys.hdes.client.test.config.TestUtils;

@QuarkusTest
@TestProfile(PgProfile.class)
public class PgComposerTest extends PgTestTemplate {

  @Test
  public void readWriteRunTest() {
    final var client = getClient().repo().repoName("PgComposerTest").create()
        .await().atMost(Duration.ofMinutes(1));
    final var composer = new HdesComposerImpl(client);       

    composer.create(ImmutableCreateEntity.builder()
        .addBody(
            ImmutableAstCommand.builder()
              .type(AstCommandValue.SET_BODY)
              .value(FileUtils.toString(getClass(), "pg_test/pg-aml-flow.txt"))
        .build())
        .type(AstBodyType.FLOW)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    composer.create(ImmutableCreateEntity.builder()
        .body(getCommands("pg_test/pg-dt.json"))
        .type(AstBodyType.DT)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    composer.create(ImmutableCreateEntity.builder()
        .addBody(
            ImmutableAstCommand.builder()
              .type(AstCommandValue.SET_BODY)
              .value(FileUtils.toString(getClass(), "pg_test/PgTestService.txt"))
              .build())
        .type(AstBodyType.FLOW_TASK)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    
    
    final var state = composer.create(ImmutableCreateEntity.builder()
        .name("first-tag")
        .name("first-tag-desc")
        .type(AstBodyType.TAG)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    final var tag = state.getTags().values().iterator().next();
    Assertions.assertEquals(tag.getAst().getValues().size(), 3);
    Assertions.assertNotNull(HdesInMemoryStore.builder().build(tag.getAst()));
    
    final var oldRelease = getRelease("release_without_id.json");
    Assertions.assertEquals(oldRelease.getValues().size(), 3);
    Assertions.assertNotNull(HdesInMemoryStore.builder().build(oldRelease));
  }
  
  
  public static List<AstCommand> getCommands(String fileName) {
    try {
      final var data = FileUtils.toString(PgComposerTest.class, fileName);
      return TestUtils.client.mapper().commandsList(data);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public static AstTag getRelease(String fileName) {
    try {
      final var data = FileUtils.toString(PgComposerTest.class, fileName);
      return TestUtils.objectMapper.readValue(data, AstTag.class);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
