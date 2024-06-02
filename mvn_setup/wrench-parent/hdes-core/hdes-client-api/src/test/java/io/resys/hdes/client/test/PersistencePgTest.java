package io.resys.hdes.client.test;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableDeleteAstType;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.spi.util.RepositoryToStaticData;
import io.resys.hdes.client.test.config.PgProfile;
import io.resys.hdes.client.test.config.PgTestTemplate;

@QuarkusTest
@TestProfile(PgProfile.class)
public class PersistencePgTest extends PgTestTemplate {

  @Test
  public void basicReadWriteDeleteTest() {
    final var repo = getStore().query().repoName("basicReadWriteDeleteTest").create()
        .await().atMost(Duration.ofMinutes(1));
    
    StoreEntity article1 = repo.create(
        ImmutableCreateStoreEntity.builder().bodyType(AstBodyType.FLOW)
            .addBody(ImmutableAstCommand.builder()
            .type(AstCommandValue.SET_BODY)
            .value("id: firstFlow")
            .build())
        .build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    super.addId(article1);
    
    // create state
    var expected = RepositoryToStaticData.toString(PersistencePgTest.class, "pg-test/create_state.txt");
    var actual = super.toRepoExport(repo.getTenantId());
    Assertions.assertEquals(expected, actual);
    
    repo.update(ImmutableUpdateStoreEntity.builder()
        .id(article1.getId())
        .addBody(ImmutableAstCommand.builder()
            .type(AstCommandValue.SET_BODY)
            .value("id: change flow symbolic id")
            .build())
        .build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    
    // update state
    expected = RepositoryToStaticData.toString(getClass(), "pg-test/update_state.txt");
    actual = super.toRepoExport(repo.getTenantId());
    Assertions.assertEquals(expected, actual);
    
    
    repo.delete(ImmutableDeleteAstType.builder().bodyType(AstBodyType.FLOW).id(article1.getId()).build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    /* delete state
    expected = RepositoryToStaticData.toString(getClass(), "delete_state.txt");
    actual = super.toRepoExport(repo.getTenantId());
    Assertions.assertEquals(expected, actual);
    */
  }

}
