package io.resys.hdes.client.test;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.resys.hdes.client.spi.util.FileUtils;
import io.resys.hdes.client.test.config.PgProfile;
import io.resys.hdes.client.test.config.PgTestTemplate;
import io.resys.hdes.client.test.config.TestUtils;

@QuarkusTest
@TestProfile(PgProfile.class)
public class PgComposerTest extends PgTestTemplate {

  @Test
  public void readWriteRunTest() {
    final var repo = getStore().query().repoName("basicReadWriteDeleteTest").create()
        .await().atMost(Duration.ofMinutes(1));
    final var composer = new HdesComposerImpl(TestUtils.client, repo);       

    composer.create(ImmutableCreateEntity.builder()
        .addBody(
            ImmutableAstCommand.builder()
              .type(AstCommandValue.SET_BODY)
              .value(FileUtils.toString(getClass(), "pg-test/pg-aml-flow.txt"))
        .build())
        .type(AstBodyType.FLOW)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    composer.create(ImmutableCreateEntity.builder()
        .body(getCommands("pg-test/pg-dt.json"))
        .type(AstBodyType.DT)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    composer.create(ImmutableCreateEntity.builder()
        .addBody(
            ImmutableAstCommand.builder()
              .type(AstCommandValue.SET_BODY)
              .value(FileUtils.toString(getClass(), "pg-test/PgTestService.txt"))
              .build())
        .type(AstBodyType.FLOW_TASK)
        .build())
    .await().atMost(Duration.ofMinutes(1));
    
    

  }
  
  
  public static List<AstCommand> getCommands(String fileName) {
    try {
      final var data = FileUtils.toString(PgComposerTest.class, fileName);
      return TestUtils.client.mapper().commandsList(data);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

}
