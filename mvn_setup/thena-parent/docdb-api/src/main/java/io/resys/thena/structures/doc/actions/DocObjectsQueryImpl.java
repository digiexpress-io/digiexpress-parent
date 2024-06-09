package io.resys.thena.structures.doc.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.DocQueryActions.Branches;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.actions.DocQueryActions.IncludeInQuery;
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
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DocObjectsQueryImpl implements DocObjectsQuery {
  public static String BRANCH_MAIN = Branches.main.name();
  private final DbState state;
  private final String repoId;
  private final List<IncludeInQuery> include = new ArrayList<>();
  private final List<String> docType = new ArrayList<>();
  private String branchName;
  private String parentId;
  private String ownerId;
 
  @Override public DocObjectsQuery ownerId(String ownerId) { this.ownerId = ownerId; return this; }
  @Override public DocObjectsQuery parentId(String parentId) { this.parentId = parentId; return this; }
  @Override public DocObjectsQuery branchName(String branchName) { this.branchName = branchName; return this; }
  @Override public DocObjectsQuery include(IncludeInQuery ... children) { this.include.addAll(Arrays.asList(children)); return this; }
  @Override public DocObjectsQuery docType(String ...docType) {
    this.docType.addAll(Arrays.asList(docType));
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
  
  public Uni<QueryEnvelope<DocObject>> get(Optional<String> id, boolean failOnNotFound) {
    final var filterBuilder = ImmutableDocFilter.builder()
        .docTypes(docType)
        .parentId(parentId)
        .ownerId(ownerId)
        .branch(branchName);
    
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
        .docTypes(docType)
        .branch(branchName)
        .parentId(parentId)
        .ownerId(ownerId)
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
