package io.resys.thena.structures.grim.create;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableObject;

import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.tuples.Tuple2;
import io.vertx.core.json.JsonObject;

public class NewMissionLabelBuilder implements ThenaGrimNewObject.NewLabel {
  private final GrimCommitBuilder logger;
  private final String missionId;

  private final Map<String, GrimMissionLabel> all_missionLabels;
  private final Map<String, GrimLabel> all_labels;
 
  private boolean built;
  private String labelValue;
  private String labelType;
  private JsonObject labelBody;
  private GrimOneOfRelations rels;
  
  public NewMissionLabelBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimMissionLabel> all_missionLabels,
      Map<String, GrimLabel> all_labels) {
    
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.rels = relation;
    this.all_missionLabels = all_missionLabels;
    this.all_labels = all_labels;
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
  public Tuple2<ImmutableGrimMissionLabel, Optional<GrimLabel>> close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize mission CREATE or UPDATE!");
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    
    final MutableObject<GrimLabel> getOrCreateLabel = new MutableObject<>();
    
    final var label = this.all_labels.values().stream()
      .filter(a -> a.getLabelValue().equals(labelValue) ||  a.getId().equals(labelValue))
      .findFirst().orElseGet(() -> {
        RepoAssert.notEmpty(labelType, () -> "labelType must be defined!");
        final var newLabel = ImmutableGrimLabel.builder()
            .id(OidUtils.gen())
            .commitId(logger.getCommitId())
            .labelValue(labelValue)
            .labelType(labelType)
            .labelBody(labelBody)
            .build();
        getOrCreateLabel.setValue(newLabel);
        logger.add(newLabel);
        return newLabel;
      });
    
    
    final ImmutableGrimMissionLabel built = ImmutableGrimMissionLabel.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .labelId(label.getId())
        .relation(rels)
        .build();
    
    RepoAssert.isTrue(
        this.all_missionLabels.values().stream()
        .filter(a -> 
          (a.getRelation() == null && rels == null) ||
          (a.getRelation() != null && a.getRelation().equals(rels))
        )
        .filter(a -> 
          a.getLabelId().equals(built.getLabelId())
        )
        .count() == 0
        , () -> "can't have duplicate assignments!");
    
    logger.add(built);
    return Tuple2.of(built, Optional.ofNullable(getOrCreateLabel.getValue()));
  }
}
