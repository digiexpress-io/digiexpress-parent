package io.resys.thena.api.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.thena.api.entities.DocContainer;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.smallrye.mutiny.Uni;

public interface DocQueryActions {
  DocObjectsQuery docQuery();

  
  interface DocObjectsQuery {
    DocObjectsQuery matchId(String matchId);          // can be external id/doc id
    DocObjectsQuery matchIds(List<String> matchId);   // can be external id/doc id
    DocObjectsQuery branchName(String branchName);
    DocObjectsQuery docType(String docType);
    DocObjectsQuery children(boolean includeChildren);
    
    Uni<QueryEnvelope<DocObject>> get();
    Uni<QueryEnvelope<DocObjects>> findAll();
  }


  @Value.Immutable
  interface DocObject extends DocContainer {
    Doc getDoc();
    Map<String, DocBranch> getBranches();
    Map<String, List<DocLog>> getLogs();        // branch id - latest commit logs 
    Map<String, DocCommit> getCommits();        // branch id - latest commit
    
    default <T> List<T> accept(DocObjectsVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      final var doc = getDoc();
      for(final var branch : getBranches().values()) {
        final T value = visitor.visit(doc, branch, getCommits().get(branch.getId()), getLogs().get(branch.getCommitId()));
        result.add(value);
      }
    
      return Collections.unmodifiableList(result);
    }
  }


  @Value.Immutable
  interface DocObjects extends DocContainer {
    List<Doc> getDocs();
    Map<String, List<DocBranch>> getBranches(); // doc id    - list of document branches 
    Map<String, DocCommit> getCommits();        // branch id - latest commit
    Map<String, List<DocLog>> getLogs();        // branch id - latest commit logs
    
    default <T> List<T> accept(DocContainer.DocObjectsVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      for(final var doc : getDocs()) {
        for(final var branch : getBranches().get(doc.getId())) {
          final T value = visitor.visit(doc, branch, getCommits().get(branch.getCommitId()), getLogs().get(branch.getCommitId()));
          result.add(value);
        }
      }
      return Collections.unmodifiableList(result);
    }
  }


  @Value.Immutable
  interface DocBranchObject extends DocContainer {
    Doc getDoc();
    DocBranch getBranch();
    DocCommit getCommit();
    List<DocLog> getLogs();
  }
  
  /* Probably no need
   * 
   *
  DocBranchObjectsQuery docBranchQuery();
  interface DocBranchObjectsQuery {
    DocBranchObjectsQuery repoId(String repoId);
    DocBranchObjectsQuery matchId(String matchId);    // can be external id/branch id/doc id
    DocBranchObjectsQuery branchName(String branchName);
    DocBranchObjectsQuery matchBy(List<MatchCriteria> blobCriteria);
    DocBranchObjectsQuery matchBy(MatchCriteria blobCriteria);

    Uni<QueryEnvelope<DocBranchObject>> get();
    Uni<QueryEnvelope<DocObjects>> findAll();
  }*/
}