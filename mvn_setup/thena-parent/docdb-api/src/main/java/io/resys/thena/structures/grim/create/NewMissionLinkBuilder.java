package io.resys.thena.structures.grim.create;

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ThenaGrimChanges;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.LinkChanges;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

public class NewMissionLinkBuilder implements ThenaGrimChanges.LinkChanges {
  private final GrimCommitBuilder logger;
  private final @Nullable GrimOneOfRelations relation;
  private final Map<String, GrimMissionLink> all_missionLinks;
  private ImmutableGrimMissionLink.Builder next; 
  private boolean built;
  
  public NewMissionLinkBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimMissionLink> all_missionLinks) {
    
    super();
    this.logger = logger;
    this.relation = relation;
    this.next = ImmutableGrimMissionLink.builder().missionId(missionId).commitId(logger.getCommitId()).id(OidUtils.gen());
    this.all_missionLinks = all_missionLinks;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  
  @Override
  public LinkChanges linkType(String linkType) {
    this.next.linkType(linkType);
    return this;
  }
  @Override
  public LinkChanges linkValue(String linkValue) {
    this.next.externalId(linkValue);
    return this;
  }
  @Override
  public LinkChanges linkBody(JsonObject linkBody) {
    this.next.linkBody(linkBody);
    return this;
  }
  @Override
  public LinkChanges oneOfRelations(GrimOneOfRelations rels) {
    this.next.relation(rels);
    return this;
  }

  public ImmutableGrimMissionLink close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var built = next.relation(relation).build();
    
    RepoAssert.isTrue(
        this.all_missionLinks.values().stream()
        .filter(a -> 
          (a.getRelation() == null && relation == null) ||
          (a.getRelation() != null && a.getRelation().equals(relation))
        )
        .filter(a -> 
          a.getLinkType().equals(built.getLinkType()) &&
          a.getExternalId().equals(built.getExternalId())
        )
        .count() == 0
        , () -> "can't have duplicate link of type: " + built.getLinkType() + ", with value: " + built.getExternalId() + "!");
    
    this.logger.add(built);
    return built;
  }
}
