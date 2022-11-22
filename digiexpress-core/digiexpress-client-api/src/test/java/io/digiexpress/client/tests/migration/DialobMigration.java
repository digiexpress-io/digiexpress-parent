package io.digiexpress.client.tests.migration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.pgsql.PgSqlDialobStore;
import io.dialob.client.pgsql.migration.MigrationClient;
import io.dialob.client.spi.DialobComposerImpl;
import io.dialob.client.spi.migration.MigrationSupport;
import io.dialob.client.spi.support.OidUtils;
import io.dialob.pgsql.test.CreateMigrationDB.MigrationProfile;
import io.dialob.pgsql.test.config.DialobClientImplForTests;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;

@Disabled
@QuarkusTest
@Slf4j
@TestProfile(MigrationProfile.class)
public class DialobMigration {
  
  @Inject io.vertx.mutiny.pgclient.PgPool pgPool;
  MigrationClient client;
  ObjectMapper om = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  
  
  public static class MigrationProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      return MigrationClient.withProfile(Map.of(          
        "quarkus.datasource.devservices.enabled", "false",
        "quarkus.datasource.db-kind", "postgresql", 
        "quarkus.datasource.username", "postgres",
        "quarkus.datasource.password", "example",
        "quarkus.datasource.reactive.max-size", "3",
        "quarkus.datasource.reactive.url", "postgresql://localhost:5432/postgres")
      );
    }

    @Override
    public String getConfigProfile() {
      return "migration-profile";
    }
  }
  
  
  @Test
  public void downloadDataFromOldDB() throws IOException {
    final var client = new MigrationClient(pgPool);
    
    final var migration = client.getRelease("");
    LOGGER.error(migration.getLog());
    
    final var file = new File("src/test/resources/migration_dump.txt");
    if(!file.exists()) {
      file.createNewFile();
    }
    
    LOGGER.error("Created migration dump: " + file.getAbsolutePath());
   
    final var output = new FileOutputStream(file);
    client.write(migration, output);
    output.close();
  }
  

  @Test
  public void readReleaseFromDump() throws IOException {
    final var client = new MigrationClient(pgPool);
    final var file = new File("src/test/resources/migration_dump.txt");   
    final var input = new FileInputStream(file);
    final var release = client.read(input);
    
    for(final var value : release.getValues())  {
      LOGGER.error(value.getCommands());
    }
    input.close();
  }
  
  @Test
  public void uploadToNewDB() {
    final var store = PgSqlDialobStore.builder().repoName("import-release")
        .pgPool(pgPool).objectMapper(om).gidProvider((type) -> OidUtils.gen()).build()
        ;
        //.repo().repoName("import-release").create().await().atMost(Duration.ofMinutes(1));
    
    final var client = DialobClientImplForTests.builder().store(store).objectMapper(om).build();
    final var composer = new DialobComposerImpl(client);
  
    composer.importRelease(getRelease(client)).await().atMost(Duration.ofMinutes(1));
  }
    
  public FormReleaseDocument getRelease(DialobClient client) {
    try {
      final var input = new FileInputStream(new File("src/test/resources/migration_dump.txt"));   
      return new MigrationSupport(client.getConfig().getMapper()).read(input).getRelease();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
