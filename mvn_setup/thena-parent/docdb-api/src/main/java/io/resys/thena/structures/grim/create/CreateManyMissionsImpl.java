package io.resys.thena.structures.grim.create;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;

import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.ImmutableManyMissionsEnvelope;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.MissionChanges;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts.GrimBatchForOne;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateManyMissionsImpl implements CreateManyMissions {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final LinkedHashMap<List<Serializable>, Consumer<MissionChanges>> missions = new LinkedHashMap<>();
  
  @Override
  public CreateManyMissions commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateManyMissions commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateManyMissions addMission(List<? extends Serializable> newCommands, Consumer<MissionChanges> addMission) {
    RepoAssert.notNull(newCommands, () -> "newCommands can't be empty!");
    RepoAssert.notNull(addMission, () -> "addMission can't be empty!");
    missions.put(new ArrayList<Serializable>(newCommands), addMission);
    return this;
  }

  @Override
  public Uni<ManyMissionsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(missions, () -> "missions can't be empty!");

    return this.state.withGrimTransaction(tenantId, this::doInTx);
  }

  private Uni<ManyMissionsEnvelope> doInTx(GrimState tx) {
    return createRequest(tx).onItem().transformToUni(request -> createResponse(tx, request));
  }
  
  private Uni<ManyMissionsEnvelope> createResponse(GrimState tx, GrimBatchForOne request) {
    return tx.insert().batchMany(request).onItem().transform(rsp -> {
      return ImmutableManyMissionsEnvelope.builder()
          .repoId(tenantId)
          .addMessages(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();      
    });
  }
  
  private Uni<GrimBatchForOne> createRequest(GrimState tx) {
    return tx.query().labels().findAll().onItem().transform(labels -> createRequest(tx, labels));
  }
  
  private GrimBatchForOne createRequest(GrimState tx, List<GrimLabel> labels) {
    return ImmutableGrimBatchForOne.builder().build();
  }
}
