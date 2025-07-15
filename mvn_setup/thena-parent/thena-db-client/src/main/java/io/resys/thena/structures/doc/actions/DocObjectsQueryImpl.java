package io.resys.thena.structures.doc.actions;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.actions.DocQueryActions.IncludeInQuery;
import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.Doc.DocFilter;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.ImmutableDocFilter;
import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.ImmutableDocObject;
import io.resys.thena.api.envelope.ImmutableDocTenantObjects;
import io.resys.thena.api.envelope.ImmutableQueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.DocNotFoundException;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.api.envelope.ThenaContainer;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DocObjectsQueryImpl implements DocObjectsQuery {
  public static String BRANCH_MAIN = "main";
  private final DbState state;
  private final String repoId;
  private final List<IncludeInQuery> include = new ArrayList<>();
  private final List<String> subStatus = new ArrayList<>();
  private String branchName;
  private String docType;
  private String parentId;
  private String ownerId;
  private String branchValueName;
  private String branchValueStatus;
  private Boolean emptyBranchBody;
 
  @Override
  public DocObjectsQuery emptyBranchBody() {
    this.emptyBranchBody = true;
    return this;
  }
  @Override
  public DocObjectsQuery include(IncludeInQuery... children) {
    this.include.addAll(Arrays.asList(children));
    return this;
  }
  @Override
  public DocObjectsQuery docSubStatus(String... subStatus) {
    this.subStatus.addAll(Arrays.asList(subStatus));
    return this;
  }
  @Override
  public DocObjectsQuery docSubStatus(List<String> subStatus) {
    if(subStatus != null) {
      this.subStatus.addAll(subStatus);
    }
    return this;
  }
  @Override
  public Uni<QueryEnvelope<DocObject>> get() {
    return get(Optional.empty(), true);
  }

  @Override
  public Uni<QueryEnvelope<DocObject>> get(String id) {
    return get(Optional.of(id), true);
  }
  
  @Override
  public Uni<QueryEnvelope<DocObject>> findOne() {
    return get(Optional.empty(), false);
  }
  
  @Override
  public Uni<QueryEnvelope<DocObject>> findOne(String id) {
    return get(Optional.of(id), false);
  }
  
  public Uni<QueryEnvelope<DocObject>> get(Optional<String> id, boolean failOnNotFound) {
    final var filterBuilder = ImmutableDocFilter.builder()
        .docType(docType)
        .docParentId(parentId)
        .docOwnerId(ownerId)
        .docSubStatus(subStatus)
        .branchValueEmpty(emptyBranchBody)
        .branchNameOrId(branchName)
        .branchValueName(branchValueName)
        .branchValueStatus(branchValueStatus);
    
    if(id.isPresent()) {
      filterBuilder.docIds(Arrays.asList(id.get()));
    }
    
    final DocFilter filter = filterBuilder.build();
    
    return state.toDocState(repoId).onItem().transformToUni(docState -> {
      final var tenant = docState.getDataSource().getTenant();
      
      // Query commits only on demand
      final Uni<List<DocCommit>> commits = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMITS) ?
          docState.query().commits().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      // Query trees only on demand
      final Uni<List<DocCommitTree>> trees = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMIT_TREE) ?
          docState.query().trees().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      // Query commands only on demand
      final Uni<List<DocCommands>> commands = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMANDS) ?
          docState.query().commands().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      return Uni.combine().all().unis(
          docState.query().docs().findAll(filter).collect().asList(),
          docState.query().branches().findAll(filter).collect().asList(),
          commits, trees, commands
      ).asTuple()
      .onItem().transform(data -> {
          if(data.getItem2().isEmpty()) {
            if(failOnNotFound) {
              return docNotFound(tenant, new DocNotFoundException());
            }
            return ImmutableQueryEnvelope.<DocObject>builder()
                .repo(tenant)
                .status(QueryEnvelopeStatus.OK)
                .objects(null)
                .build();
          }
          final var docIds = data.getItem2().stream().map(d -> d.getDocId() + "::" + d.getBranchName()).collect(Collectors.toSet());
          if(data.getItem2().size() > 1) {
            return docUnexpected(tenant, docIds);
          }
          
          final var objects = toDocObject(data.getItem1(), data.getItem2(), data.getItem3(), data.getItem4(), data.getItem5());
          return ImmutableQueryEnvelope.<DocObject>builder()
              .repo(tenant)
              .status(QueryEnvelopeStatus.OK)
              .objects(objects)
              .build();
        });
    });
  }
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> findAll(List<String> docs) {
    final DocFilter filter = ImmutableDocFilter.builder()
        .docIds(docs)
        .docType(docType)
        .branchNameOrId(branchName)
        .docParentId(parentId)
        .docOwnerId(ownerId)
        .docSubStatus(subStatus)
        .branchValueEmpty(emptyBranchBody)
        .branchValueName(branchValueName)
        .branchValueStatus(branchValueStatus)
        .build();
    return state.toDocState(repoId).onItem().transformToUni(docState -> {
      final var tenant = docState.getDataSource().getTenant();
      
      // Query commits only on demand
      final Uni<List<DocCommit>> commits = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMITS) ?
          docState.query().commits().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      // Query trees only on demand
      final Uni<List<DocCommitTree>> trees = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMIT_TREE) ?
          docState.query().trees().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      // Query commands only on demand
      final Uni<List<DocCommands>> commands = this.include.contains(IncludeInQuery.ALL) || this.include.contains(IncludeInQuery.COMMANDS) ?
          docState.query().commands().findAll(filter).collect().asList() :
          Uni.createFrom().item(Collections.emptyList());
      
      return Uni.combine().all().unis(
          docState.query().docs().findAll(filter).collect().asList(),
          docState.query().branches().findAll(filter).collect().asList(),
          commits, trees, commands
      ).asTuple()
      .onItem().transform(data -> {
          final var objects = toDocObjects(data.getItem1(), data.getItem2(), data.getItem3(), data.getItem4(), data.getItem5());
          return ImmutableQueryEnvelope.<DocTenantObjects>builder()
              .repo(tenant)
              .status(QueryEnvelopeStatus.OK)
              .objects(objects)
              .build();
        });
    });
  }
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> findAll() {
    return findAll(null);
  }
  
  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> deleteAll(List<String> idOrExternalIdOrName) {
    
    // lets query all docs, before deleting them, if success return all 
    
    return this.findAll(idOrExternalIdOrName).onItem().transformToUni(found -> {
      
      final var scope = ImmutableTxScope.builder()
          .commitAuthor("")
          .commitMessage("delete tx, nothing is going to be left anyway")
          .tenantId(repoId)
          .build();
      
      return this.state.withDocTransaction(scope, tx -> tx.query().docs().deleteAll(idOrExternalIdOrName)
        .onItem().transform(deleted -> {
          if(deleted.getStatus() == BatchStatus.OK) {
            return ImmutableQueryEnvelope.<DocTenantObjects>builder()
                .from(found)
                .addAllMessages(deleted.getMessages())
                .build();
          } 
          
          return ImmutableQueryEnvelope.<DocTenantObjects>builder()
              .from(found)
              .objects(null)
              .addAllMessages(deleted.getMessages())
              .status(QueryEnvelopeStatus.ERROR)
              .build();
        })
      );
    });
    

  }
  
  private <T extends ThenaContainer> QueryEnvelope<T> docNotFound(Tenant existing, DocNotFoundException ex) {
    final var msg = new StringBuilder()
      .append("Document not found by given id, from repo: '").append(existing.getId()).append("'!")
      .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
  
  private <T extends ThenaContainer> QueryEnvelope<T> docUnexpected(Tenant existing, Set<String> unexpected) {
    final var msg = new StringBuilder()
      .append("Expecting: '1' document, but found: '").append(unexpected.size()).append("'")
      .append(", from repo: '").append(existing.getId()).append("'!")
      .toString();
    return QueryEnvelope.docUnexpected(existing, log, msg);
  }
  

  private DocTenantObjects toDocObjects(
      List<Doc> docs,
      List<DocBranch> branches,
      List<DocCommit> commits,
      List<DocCommitTree> trees,
      List<DocCommands> commands) { 
    
    return ImmutableDocTenantObjects.builder()
        .docs(docs.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .branches(branches.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commands(commands.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commitTrees(trees.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commits(commits.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .build();
  }
  
  private DocObject toDocObject(
      List<Doc> docs,
      List<DocBranch> branches,
      List<DocCommit> commits,
      List<DocCommitTree> trees,
      List<DocCommands> commands) { 
    
    return ImmutableDocObject.builder()
        .doc(docs.iterator().next())
        .branches(branches.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commands(commands.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commitTrees(trees.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .commits(commits.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
        .build();
  }

}
