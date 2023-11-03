package io.dialob.pgsql.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/*-
 * #%L
 * stencil-persistence
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.Duration;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.dialob.client.api.DialobClient;
import io.dialob.client.api.DialobDocument.FormReleaseDocument;
import io.dialob.client.api.ImmutableCreateComposerRelease;
import io.dialob.client.spi.DialobComposerImpl;
import io.dialob.client.spi.migration.MigrationSupport;
import io.dialob.client.spi.support.RepositoryToStaticData;
import io.dialob.pgsql.test.config.DialobClientImplForTests;
import io.dialob.pgsql.test.config.PgProfile;
import io.dialob.pgsql.test.config.PgTestTemplate;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.extern.slf4j.Slf4j;

@QuarkusTest
@TestProfile(PgProfile.class)
@Slf4j
public class ImportReleasePgTest extends PgTestTemplate {
  private final boolean write_out = false;
  private final boolean log_out = true;
  
  @Test
  public void importIntoPGSQLTest() throws IOException {
    // import dump based on old version
    final var store = repo().repoName("import-release").create().await().atMost(Duration.ofMinutes(1));
    final var client = DialobClientImplForTests.builder().store(store).build();
    final var composer = new DialobComposerImpl(client);
    
    // creates missing entities
    final var src = getRelease(client);
    composer.importRelease(src)
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    final var expected = RepositoryToStaticData.toString(getClass(), "import_state.txt");
    final var actual = super.toRepoExport(store.getRepoName());
    Assertions.assertEquals(expected, actual);
    
    
    if(log_out) {
      final var data = composer.get().await().atMost(Duration.ofMinutes(1));
          
      for(final var value : data.getRevs().values().stream().sorted((a, b) -> a.getName().compareTo(b.getName())).collect(Collectors.toList())) {
        if(value.getName().equals("Yleinen viesti")) {
          final var generalMsg = data.getForms().get(value.getHead());
          LOGGER.error(objectMapper.writeValueAsString(generalMsg.getData()));
        }
        LOGGER.error(value.getName());
      }
    }
    
    if(write_out) {
      final var releaseFromImportedForms = composer.create(ImmutableCreateComposerRelease.builder()
          .name("demo-forms")
          .description("from dev envir")
          .build()).await().atMost(Duration.ofMinutes(1));
      
      final var file = new File("src/test/resources/release_dump.txt");
      if(!file.exists()) {
        file.createNewFile();
      }
      LOGGER.error("Created release, hash: " + releaseFromImportedForms.getHash() + ", dump: " + file.getAbsolutePath());    
      final var output = new FileOutputStream(file);
      output.write(releaseFromImportedForms.getContent().getBytes(StandardCharsets.UTF_8));
      output.close();
    }
  }
  
  public FormReleaseDocument getRelease(DialobClient client) {
    try {
      final var input = new FileInputStream(new File("src/test/resources/migration_dump2.txt"));   
      return new MigrationSupport(client.getConfig().getMapper()).read(input).getRelease();
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
