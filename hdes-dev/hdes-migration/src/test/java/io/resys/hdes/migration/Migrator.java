package io.resys.hdes.migration;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.HdesStoreFileImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Migrator {
  private final static Duration atMost = Duration.ofMillis(10000);
  
  @Test
  public void loadAndCreateRelease() throws JsonGenerationException, JsonMappingException, IOException {
    // create file-system DB
    final var store = HdesStoreFileImpl.builder()
      .objectMapper(MigratorConfigs.objectMapper)
      .repoName(MigratorConfigs.getRepoName())
      .db(MigratorConfigs.getFolderFromClasspath())
      .build();
    store.repo().repoName(store.getRepoName()).create().await().atMost(atMost);
    
    final var client = MigratorConfigs.createClient(store);
    final var composer = new HdesComposerImpl(client);
    
    // loaded assets into DB
    store.batch(MigratorConfigs.createBatch()).await().atMost(atMost);
    
    // Create a release for later import
    final var releaseState = composer
      .create(ImmutableCreateEntity.builder()
          .type(AstBodyType.TAG)
          .name("migration-release")
          .desc("first release contains only migrated assets")
          .build())
      .await().atMost(atMost);
    
    // log
    final var release = releaseState.getTags().values().iterator().next();
    final var migrationFile = new File("src/test/resources/" + MigratorConfigs.getRootName(), "release.json");
    migrationFile.delete();
    migrationFile.createNewFile();
    MigratorConfigs.objectMapper.writeValue(migrationFile, release);
    log.debug("release: {} created to: {}", release.getId(), migrationFile.getAbsolutePath());
    
    
  }
}
