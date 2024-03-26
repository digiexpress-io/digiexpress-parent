package io.resys.thena.docdb.api.actions;

import java.time.LocalDateTime;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObject;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObjects;
import io.smallrye.mutiny.Uni;

public interface PullActions {

  PullObjectsQuery pullQuery();
  
  interface PullObjectsQuery {
    PullObjectsQuery branchNameOrCommitOrTag(String branchNameOrCommitOrTag);
    PullObjectsQuery docId(List<String> blobName);
    PullObjectsQuery docId(String blobName);
    PullObjectsQuery matchBy(List<MatchCriteria> blobCriteria);
    PullObjectsQuery matchBy(MatchCriteria blobCriteria);
    Uni<QueryEnvelope<PullObject>> get();
    Uni<QueryEnvelope<PullObjects>> findAll();
  }
  
  
  @Value.Immutable  
  interface MatchCriteria {
    MatchCriteriaType getType();
    String getKey();
    
    @Nullable String getValue();
    @Nullable LocalDateTime getTargetDate();
    
    public static MatchCriteria like(String documentField, String valueToMatch) {
      return ImmutableMatchCriteria.builder()
      .key(documentField).value(valueToMatch)
      .type(MatchCriteriaType.LIKE)
      .build();
    }
    
    public static MatchCriteria equalsTo(String documentField, String valueToMatch) {
      return ImmutableMatchCriteria.builder()
      .key(documentField).value(valueToMatch)
      .type(MatchCriteriaType.EQUALS)
      .build();
    }
    public static MatchCriteria notNull(String documentField) {
      return ImmutableMatchCriteria.builder()
      .key(documentField)
      .type(MatchCriteriaType.NOT_NULL)
      .build();
    }
    
    public static MatchCriteria greaterThanOrEqualTo(String documentField, LocalDateTime valueToMatch) {
      return ImmutableMatchCriteria.builder()
      .key(documentField)
      .type(MatchCriteriaType.GTE)
      .targetDate(valueToMatch)
      .build();
    }
  }
  
  enum MatchCriteriaType {
    EQUALS, LIKE, NOT_NULL, 
    
    //Greater Than or Equal to
    GTE;
    

  }
}
