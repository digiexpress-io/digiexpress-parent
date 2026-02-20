package io.digiexpress.eveli.mig.v6.assets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.mig.v6.assets.CommitNode.CommitNodeType;
import io.digiexpress.eveli.mig.v6.assets.CommitNodeBuilder.CommitNode_Impl;
import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import lombok.Value;

public class AssetMerger {
  private OldGit.OldGitObjects stencil;
  private OldGit.OldGitObjects wrench;
  private OldEnvir.OldEnvirObjects envir;
  private final List<String> released = new ArrayList<>();
  
  
  public AssetMerger stencil(OldGit.OldGitObjects stencil) {
    this.stencil = stencil;
    return this;
  }
  public AssetMerger wrench(OldGit.OldGitObjects wrench) {
    this.wrench = wrench;
    return this;
  }
  public AssetMerger envir(OldEnvir.OldEnvirObjects envir) {
    this.envir = envir;
    return this;
  }

  public AssetMergerResult build() {
    
    final var stencil_root = new CommitNodeBuilder(CommitNodeType.STENCIL, this.stencil).build();
    final var wrench_root = new CommitNodeBuilder(CommitNodeType.WRENCH, this.wrench).build();
    
    CommitNode previous = null;
    final var iterator = new CommitIterator(Arrays.asList(stencil_root, wrench_root));
    while(iterator.isNext()) {
      final var next = iterator.next();
      createCommit(next);
      createTags(previous, next);
      previous = next;
    }
    
    final var unclaimedTags = this.envir.getDocs().stream()
      .filter(doc -> !this.released.contains(doc.getId()))
      .sorted((a, b) -> this.envir.getCommit(a).getCreatedAt().compareTo(this.envir.getCommit(b).getCreatedAt()))
      .toList();
    for(final var tag : unclaimedTags) {
      createTag(tag);
    }
    
    return new AssetMergerResult();
  }
  private void createTags(CommitNode previous, CommitNode next) {
    final var startDate = Optional.ofNullable(previous)
        .map(e -> e.getCommit().getDatetime())
        .orElseGet(() -> LocalDateTime.MIN);
    final var endDate = next.getCommit().getDatetime();
    
    final var tags = this.envir.getDocs().stream()
      .filter(doc -> !this.released.contains(doc.getId()))
      .filter(doc -> {
        final var targetDate = this.envir.getCommit(doc).getCreatedAt().toLocalDateTime();
        boolean isWithinRange = !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
        return isWithinRange;
      })
      .collect(Collectors.toList());
    
    if(tags.isEmpty()) {
      return;
    }

    for(final var tag : tags) {
      createTag(tag);
    }
  }
  
  
  private void createCommit(CommitNode_Impl node) {
    System.out.println(node.getType().name() +  ", at: " + node.getCommit().getDatetime());
  }
  
  private void createTag(OldEnvir.Doc doc) {
    final var commit = envir.getCommit(doc);
    System.out.println("Tag, at: " + commit.getCreatedAt().toLocalDateTime());
    
    
    this.released.add(doc.getId());
  }
  

  @Value
  public static class AssetMergerResult {
    
  }
  
  
  public static class MergedAsset {
    
  }
}
