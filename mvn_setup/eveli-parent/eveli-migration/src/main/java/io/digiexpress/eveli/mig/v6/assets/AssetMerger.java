package io.digiexpress.eveli.mig.v6.assets;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.mig.v6.assets.ExtractedNode.ExtractedNodeType;
import io.digiexpress.eveli.mig.v6.assets.ExtractedNode.TemplateNode;
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
    
    final var stencil_root = new OldGitExtractor(ExtractedNodeType.STENCIL, this.stencil).build();
    final var wrench_root = new OldGitExtractor(ExtractedNodeType.WRENCH, this.wrench).build();
    
    TemplateNode previous = null;
    final var iterator = new CommitIterator(Arrays.asList(stencil_root, wrench_root));
    while(iterator.isNext()) {
      final var next = iterator.next();
      commit(next);
      tag(previous, next);
      previous = next;
    }
    
    return new AssetMergerResult();
  }
  
  private void commit(TemplateNode node) {
    System.out.println(node.getType().name() +  ", at: " + node.getCommit().getDatetime());
  }
  
  private void tag(TemplateNode previous, TemplateNode next) {
    final var startDate = Optional.ofNullable(previous)
        .map(e -> e.getCommit().getDatetime())
        .orElseGet(() -> LocalDateTime.MIN);
    final var endDate = next.getCommit().getDatetime();
    
    final var tags = this.envir.getDocs().stream()
      .filter(doc -> !this.released.contains(doc.getId()))
      .filter(doc -> {
        final var found = this.envir.getCommits().stream().filter(e -> e.getId().equals(doc.getCreatedWithCommitId())).findFirst().get();
        final var targetDate = found.getCreatedAt().toLocalDateTime();
        boolean isWithinRange = !targetDate.isBefore(startDate) && !targetDate.isAfter(endDate);
        return isWithinRange;
      })
      .collect(Collectors.toList());
    
    if(tags.isEmpty()) {
      return;
    }

    for(final var tag : tags) {
      final var commit = this.envir.getCommits().stream().filter(e -> e.getId().equals(tag.getCreatedWithCommitId())).findFirst().get();
      System.out.println("Tag, at: " + commit.getCreatedAt().toLocalDateTime());
    }
  }

  @Value
  public static class AssetMergerResult {
    
  }
}
