package io.resys.thena.api.envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;

public interface DocContainer extends ThenaContainer {

  interface DocObjectsVisitor<T> {
    T visit(
        Doc doc, DocBranch docBranch, DocCommit commit,
        // loaded on demand
        List<DocCommands> commands,
        // loaded on demand
        List<DocCommitTree> tree);
  }
  

  @Value.Immutable
  interface DocTenantObjects extends DocContainer {
    Map<String, Doc> getDocs();   
    Map<String, DocBranch> getBranches();
    Map<String, DocCommands> getCommands();
    Map<String, DocCommit> getCommits();
    Map<String, DocCommitTree> getCommitTrees();
    
    default <T> List<T> accept(DocContainer.DocObjectsVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      
      // branches for each doc
      final var branches = getBranches().values().stream()
          .collect(Collectors.groupingBy(b -> b.getDocId()));
      
      // commands ONLY for branches
      final var commands = getCommands().values().stream()
          .filter(c -> c.getBranchName().isPresent())
          .collect(Collectors.groupingBy(b -> b.getDocId() + "::" + b.getBranchName().get()));
      
      // commitTrees only for branches
      final var commitTrees = getCommitTrees().values().stream()
          .filter(c -> c.getBranchName().isPresent())
          .collect(Collectors.groupingBy(b -> b.getDocId() + "::" + b.getBranchName().get()));
      
      
      for(final var doc : getDocs().values()) {
        if(!branches.containsKey(doc.getId())) {
          continue;
        }
        
        for(final var branch : branches.get(doc.getId())) {
          final List<DocCommands> branchCommands = commands.get(branch.getDocId() + "::" + branch.getBranchName());
          final List<DocCommitTree> branchCommitTrees = commitTrees.get(branch.getDocId() + "::" + branch.getBranchName());
          final var commit = getCommits().get(branch.getCommitId());
          final T value = visitor.visit(doc, branch, commit, branchCommands, branchCommitTrees);
          result.add(value);
        }
      }
      return Collections.unmodifiableList(result);
    }
  }
  
  @Value.Immutable
  interface DocObject extends DocContainer {
    Doc getDoc();
    Map<String, DocBranch> getBranches();
    Map<String, DocCommands> getCommands();
    Map<String, DocCommit> getCommits();
    Map<String, DocCommitTree> getCommitTrees();
    
    default <T> List<T> accept(DocObjectsVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      final var doc = getDoc();
      
      // branches for each doc
      final var branches = getBranches().values().stream()
          .collect(Collectors.groupingBy(b -> b.getDocId()));
      
      // commands ONLY for branches
      final var commands = getCommands().values().stream()
          .filter(c -> c.getBranchName().isPresent())
          .collect(Collectors.groupingBy(b -> b.getDocId() + "::" + b.getBranchName().get()));
      
      // commitTrees only for branches
      final var commitTrees = getCommitTrees().values().stream()
          .filter(c -> c.getBranchName().isPresent())
          .collect(Collectors.groupingBy(b -> b.getDocId() + "::" + b.getBranchName().get()));
      
      
      for(final var branch : branches.get(doc.getId())) {
        final List<DocCommands> branchCommands = commands.get(branch.getDocId() + "::" + branch.getBranchName());
        final List<DocCommitTree> branchCommitTrees = commitTrees.get(branch.getDocId() + "::" + branch.getBranchName());
        final var commit = getCommits().get(branch.getCommitId());
        final T value = visitor.visit(doc, branch, commit, branchCommands, branchCommitTrees);
        result.add(value);
      }
    
      return Collections.unmodifiableList(result);
    }
  }
}
