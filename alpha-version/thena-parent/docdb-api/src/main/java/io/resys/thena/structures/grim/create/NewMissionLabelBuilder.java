package io.resys.thena.structures.grim.create;

import java.util.Map;

import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class NewMissionLabelBuilder implements ThenaGrimNewObject.NewLabel {
  private final GrimCommitBuilder logger;
  private final String missionId;
  private final Map<String, GrimMissionLabel> all_missionLabels;

  private boolean built;
  private String labelValue;
  private String labelType;
  private JsonObject labelBody;
  private GrimOneOfRelations rels;
  
  public NewMissionLabelBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimMissionLabel> all_missionLabels) {
    
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.rels = relation;
    this.all_missionLabels = all_missionLabels;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewLabel labelValue(String labelValue) {
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    this.labelValue = labelValue;
    return this;
  }
  @Override
  public NewLabel labelType(String labelType) {
    this.labelType = labelType;
    return this;
  }
  @Override
  public NewLabel labelBody(JsonObject labelBody) {
    this.labelBody = labelBody;
    return this;
  }
  public ImmutableGrimMissionLabel close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize mission CREATE or UPDATE!");
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    RepoAssert.notEmpty(labelType, () -> "labelType must be defined!");
    
    
    
    final ImmutableGrimMissionLabel built = ImmutableGrimMissionLabel.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .labelValue(labelValue)
        .labelType(labelType)
        .labelBody(labelBody)
        .relation(rels)
        .build();
    
    RepoAssert.isTrue(
        this.all_missionLabels.values().stream()
        .filter(a -> 
          (a.getRelation() == null && rels == null) ||
          (a.getRelation() != null && a.getRelation().equals(rels))
        )
        .filter(a -> a.getLabelType().equals(built.getLabelType()))
        .filter(a -> a.getLabelValue().equals(built.getLabelValue()))
        .count() == 0
        , () -> "can't have duplicate labels!");
    
    logger.add(built);
    return built;
  }
}
