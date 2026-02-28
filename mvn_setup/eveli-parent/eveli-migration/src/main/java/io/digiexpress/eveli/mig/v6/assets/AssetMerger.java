package io.digiexpress.eveli.mig.v6.assets;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectType;
import io.digiexpress.eveli.mig.v6.assets.CommitNode.NodeOperation;
import io.digiexpress.eveli.mig.v6.baseline.OldEnvir;
import io.digiexpress.eveli.mig.v6.baseline.OldGit;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AssetMerger {
  private OldGit.OldGitObjects stencil;
  private OldGit.OldGitObjects wrench;
  private OldEnvir.OldEnvirObjects envir;
  private final List<String> released = new ArrayList<>();
  private final List<AssetEvent> merged = new ArrayList<>();
  
  
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

  public List<AssetEvent> build() {
    
    final var stencil_root = new CommitNodeBuilder(ObjectType.STENCIL, this.stencil).build();
    final var wrench_root = new CommitNodeBuilder(ObjectType.WRENCH, this.wrench).build();
    
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
    
    return merged;
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
  
  
  private void createCommit(CommitNode node) {

    
    final var commit = ImmutableAssetEvent.builder()
        .sourceType(node.getType())
        .createdAt(node.getCommit().getDatetime().atOffset(ZoneOffset.of("+2")))
        .operations(node.getNodeOperations().stream().map(object -> {
          final var operation = mapToObjectOperation(node, object);
          return operation;
        })
            
        .toList()).build();
    
    log.info("{}, {} file(s), at: {}", node.getType().name(), commit.getOperations().size(), node.getCommit().getDatetime());    
    this.merged.add(commit);
  }
  
  private AssetEvent.ObjectOperation mapToObjectOperation(CommitNode node, NodeOperation op) {
    
    if(op.isAdd()) {
      
      final var add = op.toAdd().getAdded();
      final var objectId = add.getName();
      final var newObject = node.getSrc().getBlobs().get(add.getBlob());
      
      return ImmutableNewOperation.builder()
          .originalCommitId(node.getCommit().getId())
          .sourceTree(node.getSrc())
          .objectId(objectId)
          .newObject(newObject.getValue())
          .build();
    } else if(op.isMerge()) {

      final var merge = op.toMerge();
      final var objectId = merge.getAfter().getName();
      final var before = node.getSrc().getBlobs().get(merge.getBefore().getBlob());
      final var newObject = node.getSrc().getBlobs().get(merge.getAfter().getBlob());
      
      return ImmutableMergeOperation.builder()
          .originalCommitId(node.getCommit().getId())
          .sourceTree(node.getSrc())
          .objectId(objectId)
          .previous(before.getValue())
          .newObject(newObject.getValue())
          .build();
      
    }
    
    final var rm = op.toRm();
    final var objectId = rm.getRemoved().getName();
    final var before = node.getSrc().getBlobs().get(rm.getRemoved().getBlob());
    return ImmutableRmOperation.builder()
        .originalCommitId(node.getCommit().getId())
        .sourceTree(node.getSrc())
        .objectId(objectId)
        .previous(before.getValue())
        .build();
  }

 

  private void createTag(OldEnvir.Doc doc) {
    final var commit = envir.getCommit(doc);
    log.info("Tag, at: {}", commit.getCreatedAt().toLocalDateTime());

    final var src = envir.getBranches().stream().filter(e -> e.getDocId().equals(doc.getId())).findFirst().get();
    
    final var op = ImmutableTagOperation.builder()
        .id(doc.getId())
        .name(doc.getDocName().get())
        .author(commit.getAuthor())
        .createdAt(commit.getCreatedAt())
        .startsAt(doc.getDocStartsAt().get())
        .description(doc.getDocDescription().orElse(""))
        .errors(doc.getDocMeta().orElse(null))
        .sources(src.getValue())
        .sourceTree(envir)
        .originalCommitId(commit.getId())
        .build();
    
    final var newTag = ImmutableAssetEvent.builder()
        .createdAt(commit.getCreatedAt())
        .sourceType(ObjectType.ENVIR)
        .operations(Arrays.asList(op))
        .build();
    
    this.merged.add(newTag);
    this.released.add(doc.getId());
  }
}
