package io.resys.thena.grim.spi.actions;

import java.time.OffsetDateTime;
import java.util.List;

import io.resys.thena.api.entities.BatchStatus;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimDeletedMission;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ImmutableGrimDeletedMission;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.grim.api.GrimQueryActions.MissionDeleteQuery;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.GrimDataSource.GrimBatchMissions;
import io.resys.thena.grim.spi.GrimDataSource.GrimState;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.builders.InternalGrimInsertsImpl.GrimMissionBatchException;
import io.resys.thena.grim.spi.commitlog.GrimCommitBuilder;
import io.resys.thena.grim.spi.datasource.GrimRegistrySqlImpl;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Setter @Accessors(fluent = true, chain = true)
public class MissionDeleteQueryImpl implements MissionDeleteQuery {
  private final GrimDataSource startingState;
  private final String tenantId;
  
  private String commitAuthor;
  private String commitMessage;
  private List<String> missionId;
  
  
  @Override
  public Multi<GrimCommit> deleteAll() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
    RepoAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
    RepoAssert.notEmpty(missionId, () -> "missionId can't be empty!");

    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .tenantId(tenantId)
        .build();
    
    return startingState.withGrimTransaction(scope, this::doInTx)
        .onItem().transformToMulti(e -> Multi.createFrom().items(e.stream()));
  }
  
  private Uni<List<GrimCommit>> doInTx(GrimState state) {
    return state.missions()
        .missionId(missionId.toArray(new String[] {}))
        .onlyDocs(
            GrimDocType.GRIM_ASSIGNMENT,
            GrimDocType.GRIM_MISSION,
            
            GrimDocType.GRIM_MISSION_LINKS,
            GrimDocType.GRIM_MISSION_LABEL,
            GrimDocType.GRIM_OBJECTIVE,
            GrimDocType.GRIM_OBJECTIVE_GOAL,
            GrimDocType.GRIM_REMARK,
            GrimDocType.GRIM_COMMANDS,

            GrimDocType.GRIM_ASSIGNMENT,
            GrimDocType.GRIM_MISSION_DATA
        )
        .findAll()
        .collect().asList().onItem().transform(items -> {
          
          final var commit = new GrimCommitBuilder(tenantId, 
              ImmutableGrimCommit.builder()
              .commitId(OidUtils.gen())
              .commitAuthor(commitAuthor)
              .commitMessage(commitMessage)
              .commitLog("")
              .createdAt(OffsetDateTime.now())
              .parentCommitId(null)
              .build()    
          );
          
          for(final var item : items) {
            commit.add(addToDeleteCommit(item));
          }
          return commit.close();
        })
        .onItem().transformToUni(batch -> deleteActiveData(state, batch))
        .onItem().transform(e -> e.getCommits());
  }
  
  private GrimDeletedMission addToDeleteCommit(GrimMissionContainer container) {
    final var add = ImmutableGrimDeletedMission.builder()
      .id(container.getMission().getId())
      .mission(container.getMission())
      .missionLabels(container.getMissionLabels().values())
      .links(container.getLinks().values())
      .remarks(container.getRemarks().values())
      .objectives(container.getObjectives().values())
      .goals(container.getGoals().values())
      .data(container.getData().values())
      .assignments(container.getAssignments().values()) 
      .commands(container.getCommands().values())
      .commits(container.getCommits().values())
      .build();
  
    return add;
  }
  
  
  public Uni<GrimBatchMissions> deleteActiveData(GrimState state, GrimBatchMissions audit) {
    final ThenaSqlDataSource wrapper = (ThenaSqlDataSource) state.getDataSource();
    RepoAssert.isTrue(wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    
    final var tx = wrapper.getClient();
    final var registry = new GrimRegistrySqlImpl(wrapper.getRegistry());
    
    final var del_commands = registry.commands().deleteAllByMissionId(missionId);
    final var del_assignements = registry.assignments().deleteAllByMissionId(missionId);
    final var del_links = registry.missionLinks().deleteAllByMissionId(missionId);
    final var del_missionLabels = registry.missionLabels().deleteAllByMissionId(missionId);
    final var del_missionData = registry.missionData().deleteAllByMissionId(missionId);
    final var del_remarks = registry.remarks().deleteAllByMissionId(missionId);
    final var del_goals = registry.goals().deleteAllByMissionId(missionId);
    final var del_objectives = registry.objectives().deleteAllByMissionId(missionId);
    final var del_commit_view = registry.commitViewers().deleteAllByMissionId(missionId);
    final var del_commit_tree = registry.commitTrees().deleteAllByMissionId(missionId);
    final var del_mission = registry.missions().deleteAllByMissionId(missionId);
    final var del_commit = registry.commits().deleteAllByMissionId(missionId);
    
    
    final var ins_commit = registry.commits().insertAll(audit.getCommits());
    final var ins_commitTree = registry.commitTrees().insertAll(audit.getCommitTrees());
    
    
    final var init = ImmutableGrimBatchMissions.builder()
        .from(audit)
        .tenantId(wrapper.getTenant().getId())
        .status(BatchStatus.OK)
        .log("")
        .build();
    
    final Uni<GrimBatchMissions> del_commands_uni = Execute.apply(tx, del_commands).onItem()
        .transform(row -> successOutput(init, "Commands deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete commands \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_assignements_uni = Execute.apply(tx, del_assignements).onItem()
        .transform(row -> successOutput(init, "Assignments deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete assignments \r\n" + missionId, e));

    final Uni<GrimBatchMissions> del_links_uni = Execute.apply(tx, del_links).onItem()
        .transform(row -> successOutput(init, "Links deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete links \r\n" + missionId, e));

    final Uni<GrimBatchMissions> del_missionLabels_uni = Execute.apply(tx, del_missionLabels).onItem()
        .transform(row -> successOutput(init, "Labels deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete labels \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_missionData_uni = Execute.apply(tx, del_missionData).onItem()
        .transform(row -> successOutput(init, "Mission data deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete mission data \r\n" + missionId, e));
        
    final Uni<GrimBatchMissions> del_remarks_uni = Execute.apply(tx, del_remarks).onItem()
        .transform(row -> successOutput(init, "Remarks deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete remarks \r\n" + missionId, e));

    final Uni<GrimBatchMissions> del_goals_uni = Execute.apply(tx, del_goals).onItem()
        .transform(row -> successOutput(init, "Goals deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete goals \r\n" + missionId, e));

    final Uni<GrimBatchMissions> del_objectives_uni = Execute.apply(tx, del_objectives).onItem()
        .transform(row -> successOutput(init, "Objectives deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete objectives \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_commit_view_uni = Execute.apply(tx, del_commit_view).onItem()
        .transform(row -> successOutput(init, "Commit views deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete commit views \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_commit_tree_uni = Execute.apply(tx, del_commit_tree).onItem()
        .transform(row -> successOutput(init, "Commit trees deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete commit trees \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_mission_uni = Execute.apply(tx, del_mission).onItem()
        .transform(row -> successOutput(init, "Missions deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete missions \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> del_commit_uni = Execute.apply(tx, del_commit).onItem()
        .transform(row -> successOutput(init, "Commits deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to delete commits \r\n" + missionId, e));
    

    final Uni<GrimBatchMissions> ins_commit_uni = Execute.apply(tx, ins_commit).onItem()
        .transform(row -> successOutput(init, "Commits inserted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to insert commits \r\n" + missionId, e));
    
    final Uni<GrimBatchMissions> ins_commitTree_uni = Execute.apply(tx, ins_commitTree).onItem()
        .transform(row -> successOutput(init, "Commit trees inserted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(init, "Failed to insert commit trees \r\n" + missionId, e));
    
    
    
    return Uni.combine().all()
        .unis(
          del_commands_uni,
          del_assignements_uni,
          del_links_uni,
          del_missionLabels_uni,
          del_missionData_uni,
          del_remarks_uni,
          del_goals_uni,
          del_objectives_uni,
          del_commit_view_uni,
          del_commit_tree_uni,
          del_mission_uni,
          del_commit_uni,
          
          ins_commit_uni,
          ins_commitTree_uni
        )
        .with(GrimBatchMissions.class, (List<GrimBatchMissions> items) -> merge(init, items))
        .onFailure(GrimMissionBatchException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (GrimMissionBatchException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        });
  }

  private GrimMissionBatchException failOutput(GrimBatchMissions current, String msg, Throwable t) {
    return new GrimMissionBatchException(ImmutableGrimBatchMissions.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  
  private GrimBatchMissions successOutput(GrimBatchMissions current, String msg) {
    return ImmutableGrimBatchMissions.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  private GrimBatchMissions merge(GrimBatchMissions start, List<GrimBatchMissions> current) {
    final var builder = ImmutableGrimBatchMissions.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getStatus();
    for(GrimBatchMissions value : current) {
      if(value == null) {
        continue;
      }
      
      if(status != BatchStatus.ERROR) {
        status = value.getStatus();
      }
      log.append("\r\n\r\n").append(value.getLog());
      builder.addAllMessages(value.getMessages());
    }
    
    return builder.status(status).build();
  }
}
