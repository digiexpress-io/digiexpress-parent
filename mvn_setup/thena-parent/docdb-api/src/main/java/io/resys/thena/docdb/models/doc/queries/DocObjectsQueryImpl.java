package io.resys.thena.docdb.models.doc.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.docdb.api.models.ImmutableDoc;
import io.resys.thena.docdb.api.models.ImmutableDocBranch;
import io.resys.thena.docdb.api.models.ImmutableDocCommit;
import io.resys.thena.docdb.api.models.ImmutableDocLog;
import io.resys.thena.docdb.api.models.ImmutableDocObject;
import io.resys.thena.docdb.api.models.ImmutableDocObjects;
import io.resys.thena.docdb.api.models.ImmutableQueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.DocNotFoundException;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocFlatted;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObject;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.resys.thena.docdb.api.models.ThenaEnvelope;
import io.resys.thena.docdb.models.doc.DocQueries;
import io.resys.thena.docdb.models.doc.ImmutableFlattedCriteria;
import io.resys.thena.docdb.spi.DbState;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DocObjectsQueryImpl implements DocObjectsQuery {
  private final DbState state;
  private final String repoId;
  private final ImmutableFlattedCriteria.Builder criteria = ImmutableFlattedCriteria.builder().onlyActiveDocs(true).children(false);
  
  @Override public DocObjectsQuery matchIds(List<String> matchId) { this.criteria.addAllMatchId(matchId); return this; }
  @Override public DocObjectsQuery branchName(String branchName) { this.criteria.branchName(branchName); return this; }
  @Override public DocObjectsQuery docType(String docType) { this.criteria.docType(docType); return this; }
  @Override public DocObjectsQuery children(boolean children) { this.criteria.children(children); return this; }
  
  @Override
  public DocObjectsQuery matchId(String matchId) {
    RepoAssert.notEmpty(repoId, () -> "matchId can't be empty!");
    this.criteria.addMatchId(matchId);
    return this;
  }
  
  @Override
  public Uni<QueryEnvelope<DocObject>> get() {
    final var criteria = this.criteria.build();
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return state.toDocState().query(repoId)
          .onItem().transformToMulti((DocQueries repo) -> repo.docs().findAllFlatted(criteria))
          .collect().asList()
          .onItem().transform(data -> {
            if(data.isEmpty()) {
              return docNotFound(existing, new DocNotFoundException());
            }
            final var docIds = data.stream().map(d -> d.getDocId()).collect(Collectors.toSet());
            if(docIds.size() > 1) {
              return docUnexpected(existing, docIds);
            }
            
            final var objects = toDocObject(data);
            return ImmutableQueryEnvelope.<DocObject>builder()
                .repo(existing)
                .status(QueryEnvelopeStatus.OK)
                .objects(objects)
                .build();
          });
    });
  }

  @Override
  public Uni<QueryEnvelope<DocObjects>> findAll() {
    final var criteria = this.criteria.build();
    
    return state.project().getByNameOrId(repoId)
    .onItem().transformToUni((Repo existing) -> {
      if(existing == null) {
        return Uni.createFrom().item(QueryEnvelope.repoNotFound(repoId, log));
      }
      return state.toDocState().query(repoId)
          .onItem().transformToMulti((DocQueries repo) -> repo.docs().findAllFlatted(criteria))
          .collect().asList()
          .onItem().transform(data -> ImmutableQueryEnvelope.<DocObjects>builder()
              .repo(existing)
              .status(QueryEnvelopeStatus.OK)
              .objects(toDocObjects(data))
              .build());
    });
  }
  private DocObjects toDocObjects(List<DocFlatted> data) {
    
    final var docs = new HashMap<String, Doc>();
    final var logs = new HashMap<String, List<DocLog>>();
    final var branches = new HashMap<String, List<DocBranch>>();
    final var commits = new HashMap<String, DocCommit>();
    
    for(final var entry : data) {
      if(!docs.containsKey(entry.getDocId())) {
        docs.put(entry.getDocId(), toDoc(entry));
        branches.put(entry.getDocId(), new ArrayList<>());
      }
      if(!branches.containsKey(entry.getBranchId())) {
        final var branch = toDocBranch(entry);
        logs.put(branch.getId(), new ArrayList<>());
        branches.get(entry.getDocId()).add(branch);                
      }

      if(!commits.containsKey(entry.getBranchId())) {
        final var commit = toDocCommit(entry);
        commits.put(entry.getBranchId(), commit);                
      }
      
      if(entry.getDocLogValue().isPresent()) {
        final var log = toDocLog(entry);
        logs.get(log.getBranchId()).add(log);
      }
    }
    final var builder = ImmutableDocObjects.builder();
    return builder
        .docs(docs.values())
        .branches(branches)
        .commits(commits)
        .logs(logs)
        .build(); 
  }
  
  private DocObject toDocObject(List<DocFlatted> data) {
    final var builder = ImmutableDocObject.builder();
    final var docIds = new HashSet<String>();
    final var logs = new HashMap<String, List<DocLog>>();
    final var branches = new HashMap<String, DocBranch>();
    final var commits = new HashMap<String, DocCommit>();
    for(final var entry : data) {
      if(docIds.size() == 0) {
        builder.doc(toDoc(entry));
      }
      
      docIds.add(entry.getDocId());
      if(!branches.containsKey(entry.getBranchId())) {
        final var branch = toDocBranch(entry);
        logs.put(branch.getId(), new ArrayList<>());
        branches.put(entry.getBranchId(), branch);                
      }
      if(!commits.containsKey(entry.getBranchId())) {
        final var commit = toDocCommit(entry);
        commits.put(entry.getBranchId(), commit);                
      }
      
      if(entry.getDocLogValue().isPresent()) {
        final var log = toDocLog(entry);
        logs.get(log.getBranchId()).add(log);
      }
    }
   return builder.logs(logs).commits(commits).branches(branches).build(); 
  }

  private DocLog toDocLog(DocFlatted entry) {
    return ImmutableDocLog.builder()
    .id(entry.getDocLogId().get())
    .docId(entry.getDocId())
    .branchId(entry.getBranchId())
    .docCommitId(entry.getCommitId())
    .value(entry.getDocLogValue().orElse(null))
    .build();
  }
  
  private Doc toDoc(DocFlatted entry) {
    return ImmutableDoc.builder()
    .externalId(entry.getExternalId())
    .id(entry.getDocId())
    .type(entry.getDocType())
    .meta(entry.getDocMeta().orElse(null))
    .status(entry.getDocStatus())
    .build();
  }
  
  private DocCommit toDocCommit(DocFlatted entry) {
    return ImmutableDocCommit.builder()
    .id(entry.getCommitId())
    .docId(entry.getDocId())
    .dateTime(entry.getCommitDateTime())
    .branchId(entry.getBranchId())
    .parent(entry.getCommitParent())
    .message(entry.getCommitMessage())
    .author(entry.getCommitAuthor())
    .build();
  }  
  private DocBranch toDocBranch(DocFlatted entry) {
    return ImmutableDocBranch.builder()
    .id(entry.getBranchId())
    .docId(entry.getDocId())
    .commitId(entry.getCommitId())
    .branchName(entry.getBranchName())
    .status(entry.getBranchStatus())
    .value(entry.getBranchValue())
    .build();
  }
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docNotFound(Repo existing, DocNotFoundException ex) {
    final var msg = new StringBuilder()
      .append("Document not found by given id, from repo: '").append(existing.getId()).append("'!")
      .toString();
    return QueryEnvelope.docNotFound(existing, log, msg, ex);
  }
  
  private <T extends ThenaEnvelope.ThenaObjects> QueryEnvelope<T> docUnexpected(Repo existing, Set<String> unexpected) {
    final var msg = new StringBuilder()
      .append("Expecting: '1' document, but found: '").append(unexpected.size()).append("'")
      .append(", from repo: '").append(existing.getId()).append("'!")
      .toString();
    return QueryEnvelope.docUnexpected(existing, log, msg);
  }

}
