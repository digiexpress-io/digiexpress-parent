package io.resys.hdes.client.api;

/*-
 * #%L
 * hdes-client-api
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.smallrye.mutiny.Uni;

public interface HdesStore {
  Uni<StoreEntity> create(CreateStoreEntity newType);
  Uni<StoreEntity> update(UpdateStoreEntity updateType);
  Uni<StoreEntity> delete(DeleteAstType deleteType);
  QueryBuilder query();

   
  interface QueryBuilder {
    Uni<StoreState> get();
    Uni<StoreEntity> get(String id);
  }
  
  
  
  @JsonSerialize(as = ImmutableDeleteAstType.class)
  @JsonDeserialize(as = ImmutableDeleteAstType.class)
  @Value.Immutable
  interface DeleteAstType extends Serializable {
    String getId();
  }
  
  @JsonSerialize(as = ImmutableUpdateStoreEntity.class)
  @JsonDeserialize(as = ImmutableUpdateStoreEntity.class)
  @Value.Immutable
  interface UpdateStoreEntity extends Serializable {
    String getId();
    List<AstCommand> getBody();
  }
  
  @JsonSerialize(as = ImmutableCreateStoreEntity.class)
  @JsonDeserialize(as = ImmutableCreateStoreEntity.class)
  @Value.Immutable
  interface CreateStoreEntity extends Serializable {
    AstBodyType getBodyType();
    List<AstCommand> getBody();
  }
  
  @JsonSerialize(as = ImmutableStoreState.class)
  @JsonDeserialize(as = ImmutableStoreState.class)
  @Value.Immutable
  interface StoreState {
    Map<String, StoreEntity> getTags();
    Map<String, StoreEntity> getFlows();
    Map<String, StoreEntity> getServices();
    Map<String, StoreEntity> getDecisions();
  }
  
  @JsonSerialize(as = ImmutableStoreEntity.class)
  @JsonDeserialize(as = ImmutableStoreEntity.class)
  @Value.Immutable
  interface StoreEntity {
    String getId();
    AstBodyType getBodyType();
    String getHash();
    List<AstCommand> getBody();
  }
  
  @JsonSerialize(as = ImmutableStoreExceptionMsg.class)
  @Value.Immutable
  interface StoreExceptionMsg {
    String getId();
    String getValue();
    List<String> getArgs();
  }
}
