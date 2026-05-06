package io.resys.limaone.authoring;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import io.resys.limaone.fs.WorldFs;
import io.resys.limaone.fs.WorldFsBody;
import io.resys.limaone.fs.WorldFsBody.WrenchAstBodyChange;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldDiff;
import io.resys.limaone.model.Model.ModelWorldIndex;
import io.resys.limaone.model.Model.ModelWorldSummary;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;



public interface Authoring {
  Authoring withDefaultTenant(Optional<String> tenant);
  Authoring withBranchName(Optional<String> branchName);
  
  WorldFsBodyQuery worldFsBodyQuery();
  WorldFsQuery worldFsQuery();
  WorldIndexQuery worldIndexQuery();
  WorldQuery worldQuery();
  WorldDiffQuery worldDiffQuery();
  WorldSummaryQuery worldSummaryQuery();
  WorldImport worldImport();
  WorldRefQuery worldRefQuery();
  
  TID tid();
  
  DefaultModel defaultModel();
  ModifyModel modifyModel();
  NewModel newModel();
  DeleteModel deleteModel();
  
  CopyAsModel copyAsModel();
  DebugModel debugModel();

  interface WorldFsBodyQuery {
    WorldFsBodyQuery id(String id);
    WorldFsBodyQuery bodyType(BodyType bodyType);
    WorldFsBodyQuery withTransientChanges(WrenchAstBodyChange changes); // transient, does not persist the changes
    Uni<WorldFsBody> getOne();
    WorldFsBody getOneSync();
  }
  
  interface WorldFsQuery {
    Uni<WorldFs> findAll();
    WorldFs findAllSync();
  }

  interface WorldIndexQuery {
    Multi<ModelWorldIndex> findAll();
  }
  
  interface WorldImport {
    WorldImport source(ModelWorld source);
    Uni<ModelWorld> build();
    ModelWorld buildSync();
  }
  
  interface WorldRefQuery {
    Uni<Optional<WorldRef>> findOne();
    Optional<WorldRef> findOneSync();
  }
  
  interface WorldQuery {
    WorldQuery docs(BodyType ... types); // narrow down some types... otherwise queries all
    WorldQuery commitId(UUID commitId);
    WorldQuery addObjectId(String objectId);
    Uni<ModelWorld> findAll();
    Uni<Model<?>> getOneById(String objectId);
    ModelWorld findAllSync();
  }
  
  interface WorldDiffQuery {
    WorldDiffQuery baseId(UUID baseIdCommit);
    WorldDiffQuery targetId(UUID targetIdCommit);
    Uni<ModelWorldDiff> findAll();
    ModelWorldDiff findAllSync();
  }
  
  interface WorldSummaryQuery {
    WorldSummaryQuery tagId(String tagId);
    Uni<ModelWorldSummary> findAll();
    ModelWorldSummary findAllSync();
  }
  
  
  interface ModifyModel {
    ModifyDeployment modifyDeployment();
    
    ModifyFlow modifyFlow();
    ModifyFlowTask modifyFlowTask();
    ModifyDecisionTable modifyDecisionTable();
    
    ModifyArticle modifyArticle();
    ModifyLocale modifyLocale();
    ModifyArticlePage modifyArticlePage();
    ModifyArticleLink modifyArticleLink();
    ModifyArticleWorkflow modifyArticleWorkflow();
    ModifyArticleTemplate modifyArticleTemplate();

    ModifyPrintout modifyPrintout();
    ModifyPrintoutPage modifyPrintoutPage();
    ModifyPrintoutResource modifyPrintoutResource();
  }
  
  interface NewModel {
    
    NewDeployment newDeployment();
    
    NewFlow newFlow();
    NewFlowTask newFlowTask();
    NewDecisionTable newDecisionTable();
    

    NewLocale newLocale();
    NewArticle newArticle();
    NewArticlePage newArticlePage();    
    NewArticleLink newArticleLink();
    NewArticleWorkflow newArticleWorkflow();  
    NewArticleTemplate newArticleTemplate();
    
    NewPrintout newPrintout();
    NewPrintoutPage newPrintoutPage();
    NewPrintoutResource newPrintoutResource();
  }
  

  interface DeleteModel {
    DeleteAny deleteAny();
    DeleteArticleLink deleteArticleLink();
    DeleteArticleWorkflow deleteArticleWorkflow();
    DeletePrintout deletePrintout();
    DeletePrintoutPage deletePrintoutPage();
  }
  
  interface CopyAsModel {
    CopyAny copyAny();
  }
  
  // default DT-s, FLOW-s etc...
  interface DefaultModel {
    Uni<Void> buildDefaultModel();
  }
  
  interface AuthorProps {
    @Nullable String getAuthor(); 
    @Nullable OffsetDateTime getCreatedAt();
  }
  
  
  interface DebugModel {
    DebugAny debugAny();
  }
  
  // marker interface
  interface AuthoringModelProps {
 
  }
  
  @Value.Immutable
  interface WorldRef {
    String getTenantName();
    String getBranchName();
    UUID getCommitId();
    String getHash();
  }
}
