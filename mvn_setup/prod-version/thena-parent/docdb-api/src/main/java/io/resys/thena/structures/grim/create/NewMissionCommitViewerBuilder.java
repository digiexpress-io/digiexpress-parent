package io.resys.thena.structures.grim.create;

import java.time.OffsetDateTime;

import io.resys.thena.api.entities.grim.ImmutableGrimCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMissionCommitViewer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NewMissionCommitViewerBuilder implements NewMissionCommitViewer {
  private final OffsetDateTime createdAt;
  private final String missionId;
  private final String commitId;
  
  private boolean built;
  private String userId;
  private String usedFor;
  
  @Override
  public NewMissionCommitViewer userId(String userId) {
    this.userId = userId;
    return this;
  }
  @Override
  public NewMissionCommitViewer usedFor(String usedFor) {
    this.usedFor = usedFor;
    return this;
  }
  @Override
  public void build() {
    this.built = true;
  }
  
  public ImmutableGrimCommitViewer close() {
    RepoAssert.isTrue(built, () -> "you must call MissionChanges.build() to finalize mission CREATE or UPDATE!");
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(usedFor, () -> "usedFor  must be defined!");
    
    return ImmutableGrimCommitViewer.builder()
        .id(OidUtils.gen())
        .missionId(missionId)
        .commitId(commitId)
        .createdAt(createdAt)
        .updatedAt(createdAt)
        .objectId(missionId)
        .objectType(GrimDocType.GRIM_MISSION)
        .usedBy(userId)
        .usedFor(usedFor)
        .build();
  }
}
