package io.resys.thena.structures.grim.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.GrimCommitActions.ManyCommitViewersEnvelope;
import io.resys.thena.api.actions.GrimCommitActions.ModifyManyCommitViewers;
import io.resys.thena.api.actions.ImmutableManyCommitViewersEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.GrimAnyObject;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.grim.GrimCommitViewerRegistry.AnyObjectCriteria;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts.GrimBatchForViewers;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.grim.ImmutableGrimBatchForViewers;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyManyCommitViewersImpl implements ModifyManyCommitViewers {

  private final List<NewViewers> newViewers = new ArrayList<>();
  private final Map<String, AnyObjectCriteria> criteria = new LinkedHashMap<>();
  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String usedFor;
  
  @Data @Builder @RequiredArgsConstructor
  private static class NewViewers {
    private final String commitId; 
    private final GrimDocType docType; 
    private final String objectId;
  }
  
  @Override
  public ModifyManyCommitViewers commitAuthor(String commitAuthor) {
    this.author = RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
    return this;
  }
  @Override
  public ModifyManyCommitViewers commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!"); 
    return this;
  }
  @Override
  public ModifyManyCommitViewers usedFor(String usedFor) {
    this.usedFor = RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!");
    return this;
  }
  @Override
  public ModifyManyCommitViewers object(String commitId, GrimDocType objectType, String objectId) {
    RepoAssert.notEmpty(commitId, () -> "commitId can't be empty!");
    RepoAssert.notNull(objectType, () -> "objectType can't be empty!");
    RepoAssert.notEmpty(objectId, () -> "objectId can't be empty!");
    
    if(!criteria.containsKey(objectId)) {
      criteria.put(objectId, new AnyObjectCriteria(objectId, objectType));      
    }
    if(!criteria.containsKey(commitId)) {
      criteria.put(commitId, new AnyObjectCriteria(commitId, GrimDocType.GRIM_COMMIT));      
    }
    
    RepoAssert.isTrue(newViewers.stream()
      .filter(viewer -> viewer.getDocType().equals(objectType))
      .filter(viewer -> viewer.getObjectId().equals(objectId))
      .filter(viewer -> viewer.getCommitId().equals(commitId))
      .count() == 0, () -> "Can't redefined object %s, %s, %s!".formatted(commitId, objectType, objectId));
    
    final var viewer = new NewViewers(commitId, objectType, objectId);
    newViewers.add(viewer);
    return this;
  }
  @Override
  public Uni<ManyCommitViewersEnvelope> build() {
    RepoAssert.notEmpty(author, () -> "commitAuthor can't be empty!");
    RepoAssert.notEmpty(message, () -> "commitMessage can't be empty!");
    
    RepoAssert.notEmpty(usedFor, () -> "usedFor can't be empty!");
    RepoAssert.notEmpty(criteria, () -> "object can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }

  private Uni<ManyCommitViewersEnvelope> doInTx(GrimState tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure().recoverWithItem(error -> {          
          return ImmutableManyCommitViewersEnvelope.builder()
          .repoId(tenantId).log("")
          .addMessages(ImmutableMessage.builder()
              .text(new StringBuilder()
                .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                .append(System.lineSeparator())
                .append("Message: ").append(error.getMessage())
                .toString())
              .exception(error)
              .build())
          .status(CommitResultStatus.ERROR)
          .build();
        });
  }
  
  private Uni<GrimBatchForViewers> createRequest(GrimState tx) {
    final var criteria = this.criteria.values();
    
    final var anyObjects_uni = tx.query().commitViewer().findAnyObjects(criteria).collect().asList();
    final var allViewers_uni = tx.query().commitViewer().findAllViewersByUsed(author, usedFor, this.criteria.keySet()).collect().asList();
    
    return Uni.combine().all().unis(allViewers_uni, anyObjects_uni)
        .asTuple()
        .onItem().transform(tuple -> createRequest(tx, tuple.getItem1(), tuple.getItem2()));
  }

  private String getKey(GrimCommitViewer viewer) {
    return viewer.getCommitId() + "/" + viewer.getObjectType() + "/" + viewer.getDocType();
  }
  private String getKey(NewViewers viewer) {
    return viewer.getCommitId() + "/" + viewer.getDocType() + "/" + viewer.getDocType();
  }  
  private GrimBatchForViewers createRequest(GrimState tx, List<GrimCommitViewer> viewers, List<GrimAnyObject> anyObjects) {
    final var batch = ImmutableGrimBatchForViewers
        .builder()
        .log("")
        .tenantId(tenantId)
        .status(BatchStatus.EMPTY);
    
    final var foundObjects = anyObjects.stream().collect(Collectors.toMap(e -> e.getId() , e -> e));
    final var foundViewers = viewers.stream().collect(Collectors.toMap(e -> getKey(e), e -> e));
    final var createdAt = OffsetDateTime.now();
    
    
    for(final var viewer : newViewers) {
      final var commit = foundObjects.get(viewer.getCommitId());
      if(commit == null) {
        throw new ViewerException(
            "Can't mark commit: %s, object type: %s, object id: %s viewed because there is no such commit!"
            .formatted(viewer.getCommitId(), viewer.getDocType(), viewer.getObjectId()));
      }
      final var target = foundObjects.get(viewer.getObjectId());
      if(target == null) {
        throw new ViewerException(
            "Can't mark commit: %s, object type: %s, object id: %s viewed because there is no such object!"
            .formatted(viewer.getCommitId(), viewer.getDocType(), viewer.getObjectId()));
      }
      
      final var previous = foundViewers.get(getKey(viewer));
      
      if(previous == null) {
        final var next = ImmutableGrimCommitViewer.builder()
          .commitId(viewer.getCommitId())
          .createdAt(createdAt)
          .id(OidUtils.gen())
          .objectId(viewer.getObjectId())
          .objectType(viewer.getDocType())
          .usedBy(author)
          .usedFor(usedFor)
          .missionId(target.getMissionId())
          .createdAt(createdAt)
          .updatedAt(createdAt)
          .build();
        batch.addViewers(next);
      } else {
        final var next = ImmutableGrimCommitViewer.builder().from(previous).updatedAt(createdAt).build();
        batch.addUpdateViewers(next);
      }
      batch.status(BatchStatus.OK).build();

    }
    
    return batch.build();
  }
  
  private Uni<ManyCommitViewersEnvelope> createResponse(GrimState tx, GrimBatchForViewers request) {
    if(request.getStatus() == BatchStatus.EMPTY) {
      return Uni.createFrom().item(ImmutableManyCommitViewersEnvelope.builder()
          .repoId(tenantId)
          .log("")
          .addAllViewers(request.getViewers())
          .addMessages(ImmutableMessage.builder().text("Nothing to commit, no objects to add!").build())
          .status(CommitResultStatus.NO_CHANGES)
          .build());            
    }
    
    return tx.insert().batchMany(request).onItem().transform(rsp -> {
      return ImmutableManyCommitViewersEnvelope.builder()
          .repoId(tenantId)
          .log(rsp.getLog())
          .addAllViewers(request.getViewers())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();      
    });
  }
  
  public static class ViewerException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    public ViewerException(String message) {
      super(message);
    }
  }
}
