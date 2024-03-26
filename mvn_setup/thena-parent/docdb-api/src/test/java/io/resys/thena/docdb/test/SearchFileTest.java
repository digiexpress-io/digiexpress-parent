package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.resys.thena.api.actions.ImmutableMatchCriteria;
import io.resys.thena.api.actions.PullActions.MatchCriteriaType;
import io.resys.thena.docdb.test.config.FileTestTemplate;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@QuarkusTest
public class SearchFileTest extends FileTestTemplate {

  
  public SearchFileTest() {
    super((client, repo) -> {
      
      client.git(repo).commit().commitBuilder()
          .branchName("main")
          .append("ID-1", new JsonObject(Map.of(
              "type", "person",
              "name", "sam", 
              "lastName", "vimes")))
          
          .append("ID-2", new JsonObject(Map.of(
              "type", "person",
              "name", "cassandra", 
              "lastName", "chase")))
          
          .append("ID-3", new JsonObject(Map.of(
              "type", "person",
              "name", "count", 
              "lastName", "sober")))
          
          .author("tester bob")
          .message("first commit!")
          .build()
          .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
          .await().atMost(Duration.ofMinutes(1));
      
    });
  }
  
  public void addSamVimesChanges(int changes, String id) {
    final var client = getClient();
    for(int index = 0; index < changes; index++) { 
      client.git(getRepo()).commit().commitBuilder()
      .branchName("main")
      .append(id, JsonObject.of(
        "type", "person",
        "name", "sam", "lastName", "vimes",
        "change id", (index+1) + " of changes: " + changes 
        ))
      .author("tester bob")
      .message("change commit!")
      .latestCommit()
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    }
  }
  
  public void addCassandraChaseChanges(int changes, String id) {
    final var client = getClient();
    for(int index = 0; index < changes; index++) { 
      client.git(getRepo()).commit().commitBuilder()
      .branchName("main")
      .append(id, JsonObject.of(
        "type", "person",
        "name", "cassandra", "lastName", "chase",
        "change id", (index+1) + " of changes: " + changes 
        ))
      .author("tester bob")
      .message("change commit!")
      .latestCommit()
      .build()
      .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
      .await().atMost(Duration.ofMinutes(1));
    }
  }


  @Test
  public void findByExactMatchFromAllPossibleCases() throws JSONException {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    final var history = getClient().git(getRepo()).history().blobQuery()
      .branchName("main")
      .matchBy(ImmutableMatchCriteria.builder().type(MatchCriteriaType.EQUALS).key("name").value("sam").build())
      .latestOnly()
      .get()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("Found type=person from history, body: {}", JsonArray.of(history).toString());
    Assertions.assertEquals(1, history.getObjects().getValues().size());
    
    final var first = history.getObjects().getValues().get(0);
    Assertions.assertEquals("ID-1", first.getTreeValueName());
    Assertions.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode());
  }
  
  @Test
  public void findByLikeMatchFromAllPossibleCases() {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    final var history = getClient().git(getRepo()).history().blobQuery()
      .branchName("main")
      .matchBy(ImmutableMatchCriteria.builder().type(MatchCriteriaType.LIKE).key("name").value("sam").build())
      .latestOnly()
      .get()
      .await().atMost(Duration.ofMinutes(1));
    
    log.debug("Found type=person from history, body: {}", JsonArray.of(history).toString());
    Assertions.assertEquals(1, history.getObjects().getValues().size());
    
    final var first = history.getObjects().getValues().get(0);
    Assertions.assertEquals("ID-1", first.getTreeValueName());
    Assertions.assertEquals("{\"type\":\"person\",\"name\":\"sam\",\"lastName\":\"vimes\",\"change id\":\"20 of changes: 20\"}", first.getBlob().getValue().encode());
  }
 
  
  @Test
  public void findByExactMatchAllCommits() {
    addSamVimesChanges(20, "ID-1");
    addCassandraChaseChanges(20, "ID-2");
    
    
    var history = getClient().git(getRepo()).history().blobQuery()
      .branchName("main")
      .matchBy(ImmutableMatchCriteria.builder().type(MatchCriteriaType.LIKE).key("name").value("sam").build())
      .latestOnly(false)
      .get()
      .await().atMost(Duration.ofMinutes(1));
    
    Assertions.assertEquals(41, history.getObjects().getValues().size());
    
    history = getClient().git(getRepo()).history().blobQuery()
        .branchName("main")
        .matchBy(ImmutableMatchCriteria.builder().type(MatchCriteriaType.EQUALS).key("name").value("sam").build())
        .latestOnly(false)
        .get()
        .await().atMost(Duration.ofMinutes(1));
      
    Assertions.assertEquals(41, history.getObjects().getValues().size());
  }
}
