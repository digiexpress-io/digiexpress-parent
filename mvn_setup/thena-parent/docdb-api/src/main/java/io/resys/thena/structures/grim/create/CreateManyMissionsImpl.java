package io.resys.thena.structures.grim.create;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.MissionChanges;
import io.resys.thena.spi.DbState;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateManyMissionsImpl implements CreateManyMissions {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final List<Serializable> commands = new ArrayList<>();
  
  
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
  public CreateManyMissions commitCommands(List<? extends Serializable> newCommands) {
    RepoAssert.notNull(newCommands, () -> "newCommands can't be empty!");
    this.commands.addAll(newCommands);
    return this;
  }

  @Override
  public CreateManyMissions addMission(Consumer<MissionChanges> addMission) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Uni<ManyMissionsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");

   
    return this.state.withGrimTransaction(tenantId, this::doInTx);
  }

}
