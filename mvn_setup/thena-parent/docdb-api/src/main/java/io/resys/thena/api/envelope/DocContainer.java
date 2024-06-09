package io.resys.thena.api.envelope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;

public interface DocContainer extends ThenaContainer {
  @FunctionalInterface
  interface DocOneBranchVisitor<T> {
    T visit(DocBranch docBranch);
  }
  
  @FunctionalInterface
  interface DocContainerVisitor<T> {
    T visit(
        Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit,
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
    
    default <T> List<T> accept(DocContainer.DocContainerVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      
      // branches for each doc
      final var branches = getBranches().values().stream()
          .collect(Collectors.groupingBy(b -> b.getDocId()));
      
      // commands ONLY for branches
      final var commands = getCommands().values().stream()
          .filter(c -> c.getBranchId().isPresent())
          .collect(Collectors.groupingBy(b -> b.getBranchId().get()));
      
      // commitTrees only for branches
      final var commitTrees = getCommitTrees().values().stream()
          .filter(c -> c.getBranchId().isPresent())
          .collect(Collectors.groupingBy(b -> b.getBranchId().get()));
      
      final var commits = getCommits().values().stream()
          .collect(Collectors.groupingBy(b -> b.getDocId()));
      
      
      for(final var doc : getDocs().values()) {
        if(!branches.containsKey(doc.getId())) {
          continue;
        }
        
        for(final var branch : branches.get(doc.getId())) {
          final List<DocCommands> branchCommands = commands.get(branch.getId());
          final List<DocCommitTree> branchCommitTrees = commitTrees.get(branch.getId());
          final var docCommit = Optional.ofNullable(commits.get(doc.getId()))
              .orElse(Collections.emptyList())
              .stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
          
          final T value = visitor.visit(
              doc, branch, 
              Optional.ofNullable(docCommit).orElse(Collections.emptyMap()), 
              branchCommands, branchCommitTrees);
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
    
    default <T> T accept(DocOneBranchVisitor<T> visitor) {
      if(getBranches().size() == 1) {
        return visitor.visit(getBranches().values().iterator().next());  
      }
      return null;
    }
    
    default <T> List<T> accept(DocContainerVisitor<T> visitor) {
      final var result = new ArrayList<T>();
      final var doc = getDoc();
      
      // branches for each doc
      final var branches = getBranches().values().stream()
          .collect(Collectors.groupingBy(b -> b.getDocId()));
      
      // commands ONLY for branches
      final var commands = getCommands().values().stream()
          .filter(c -> c.getBranchId().isPresent())
          .collect(Collectors.groupingBy(b -> b.getBranchId().get()));
      
      // commitTrees only for branches
      final var commitTrees = getCommitTrees().values().stream()
          .filter(c -> c.getBranchId().isPresent())
          .collect(Collectors.groupingBy(b -> b.getBranchId().get()));
      
      
      for(final var branch : branches.get(doc.getId())) {
        final List<DocCommands> branchCommands = commands.get(branch.getId());
        final List<DocCommitTree> branchCommitTrees = commitTrees.get(branch.getId());
        final var commit = getCommits();
        
        final T value = visitor.visit(doc, branch, commit, branchCommands, branchCommitTrees);
        result.add(value);
      }
    
      return Collections.unmodifiableList(result);
    }
  }
}
