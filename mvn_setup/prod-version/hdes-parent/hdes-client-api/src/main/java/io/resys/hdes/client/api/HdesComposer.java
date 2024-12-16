package io.resys.hdes.client.api;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.resys.hdes.client.api.HdesStore.HistoryEntity;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBody.AstSource;
import io.resys.hdes.client.api.ast.AstBranch;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.api.diff.TagDiff;
import io.resys.hdes.client.api.programs.Program.ProgramResult;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramAssociation;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramMessage;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.smallrye.mutiny.Uni;
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Backend for composer related service. 
 * Provides mutability of the assets.
 */
public interface HdesComposer {  
  Uni<ComposerState> get();
  Uni<ComposerEntity<?>> get(String idOrName);
  Uni<HistoryEntity> getHistory(String id);
  Uni<ComposerState> update(UpdateEntity asset);
  Uni<ComposerState> create(CreateEntity asset);
  Uni<ComposerState> importTag(AstTag asset);
  Uni<ComposerState> delete(String id);
  Uni<ComposerState> copyAs(CopyAs copyAs);
  Uni<DebugResponse> debug(DebugRequest entity);
  Uni<ComposerEntity<?>> dryRun(UpdateEntity entity);
  Uni<StoreDump> getStoreDump();
  Uni<TagDiff> diff(DiffRequest request);
  Uni<AstTagSummary> summary(String tagId);

  HdesComposer withBranch(String branchName);

  @JsonSerialize(as = ImmutableDiffRequest.class)
  @JsonDeserialize(as = ImmutableDiffRequest.class)
  @Value.Immutable
  interface DiffRequest extends Serializable {
    String getBaseId();
    String getTargetId();
  }

  @JsonSerialize(as = ImmutableDebugResponse.class)
  @JsonDeserialize(as = ImmutableDebugResponse.class)
  @Value.Immutable
  interface DebugResponse extends Serializable {
    String getId();
    @Nullable
    ProgramResult getBody();
    @Nullable
    String getBodyCsv();
  }
  
  @JsonSerialize(as = ImmutableDebugRequest.class)
  @JsonDeserialize(as = ImmutableDebugRequest.class)
  @Value.Immutable
  interface DebugRequest extends Serializable {
    String getId();
    @Nullable
    String getInput();
    @Nullable
    String getInputCSV();
  }
  
  @JsonSerialize(as = ImmutableCopyAs.class)
  @JsonDeserialize(as = ImmutableCopyAs.class)
  @Value.Immutable
  interface CopyAs extends Serializable {
    String getId(); // id of the entity
    String getName(); // new name of the entity
  }
  
  @JsonSerialize(as = ImmutableStoreDump.class)
  @JsonDeserialize(as = ImmutableStoreDump.class)
  @Value.Immutable
  interface StoreDump extends Serializable {
    String getId();
    List<AstSource> getValue();
  }

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerState.class)
  @JsonDeserialize(as = ImmutableComposerState.class)
  interface ComposerState {
    Map<String, ComposerEntity<AstTag>> getTags();
    Map<String, ComposerEntity<AstFlow>> getFlows();
    Map<String, ComposerEntity<AstService>> getServices();
    Map<String, ComposerEntity<AstDecision>> getDecisions();
    Map<String, ComposerEntity<AstBranch>> getBranches();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerEntity.class)
  @JsonDeserialize(as = ImmutableComposerEntity.class)
  interface ComposerEntity<A extends AstBody> {
    String getId();
    @Nullable
    A getAst();
    AstSource getSource();
    List<ProgramMessage> getWarnings();
    List<ProgramMessage> getErrors();
    List<ProgramAssociation> getAssociations();
    ProgramStatus getStatus();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableUpdateEntity.class)
  @JsonDeserialize(as = ImmutableUpdateEntity.class)
  interface UpdateEntity {
    String getId();
    List<AstCommand> getBody();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateEntity.class)
  @JsonDeserialize(as = ImmutableCreateEntity.class)
  interface CreateEntity {
    @Nullable
    String getName();
    @Nullable
    String getDesc();
    AstBodyType getType();
    List<AstCommand> getBody();
  }
}
