package io.dialob.pgsql.test;

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

import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobStore.StoreEntity;
import io.dialob.client.api.ImmutableCreateStoreEntity;
import io.dialob.client.api.ImmutableDeleteStoreEntity;
import io.dialob.client.api.ImmutableUpdateStoreEntity;
import io.dialob.client.spi.support.RepositoryToStaticData;
import io.dialob.pgsql.test.config.PgProfile;
import io.dialob.pgsql.test.config.PgTestTemplate;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(PgProfile.class)
public class SanityPgTest extends PgTestTemplate {

  @Test
  public void basicReadWriteDeleteTest() {
    final var repo = repo().repoName("basicReadWriteDeleteTest").create()
        .await().atMost(Duration.ofMinutes(1));
    
    StoreEntity article1 = repo.create(
        ImmutableCreateStoreEntity.builder().bodyType(DocumentType.FORM)
            .body("id: firstFlow")
            .build()
        )
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));

    // create state
    var expected = RepositoryToStaticData.toString(SanityPgTest.class, "create_state.txt");
    var actual = super.toRepoExport(repo.getRepoName());
    Assertions.assertEquals(expected, actual);
    
    StoreEntity article1_v2 = repo.update(ImmutableUpdateStoreEntity.builder()
        .id(article1.getId())
        .version(article1.getVersion())
        .body("id: change flow symbolic id")
        .bodyType(DocumentType.FORM)
        .build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    
    // update state
    expected = RepositoryToStaticData.toString(getClass(), "update_state.txt");
    actual = super.toRepoExport(repo.getRepoName());
    Assertions.assertEquals(expected, actual);
    
    
    repo.delete(ImmutableDeleteStoreEntity.builder().bodyType(DocumentType.FORM).id(article1.getId()).version(article1_v2.getVersion()).build())
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    
    // delete state
    expected = RepositoryToStaticData.toString(getClass(), "delete_state.txt");
    actual = super.toRepoExport(repo.getRepoName());
    Assertions.assertEquals(expected, actual);
    
  }
  
  

}
