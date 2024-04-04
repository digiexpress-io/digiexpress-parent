package io.resys.thena.storesql;



import java.util.List;

import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class GrimInsertsSqlImpl implements GrimInserts {
  private final ThenaSqlDataSource wrapper;
  private final GrimRegistry registry;
  
  public GrimInsertsSqlImpl(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().grim();
  }

  
  
  @Override
  public Uni<GrimBatchForOne> batchMany(GrimBatchForOne inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();

    // DELETE OPERATIONS
    final var del_assignements = registry.assignments().deleteAll(inputBatch.getDeleteAssignments());
    final var del_links = registry.missionLinks().deleteAll(inputBatch.getDeleteLinks());
    final var del_missionLabels = registry.missionLabels().deleteAll(inputBatch.getDeleteMissionLabels());
    final var del_missionData = registry.missionData().deleteAll(inputBatch.getDeleteData());
    final var del_remarks = registry.remarks().deleteAll(inputBatch.getDeleteRemarks());
    final var del_goals = registry.goals().deleteAll(inputBatch.getDeleteGoals());
    final var del_objectives = registry.objectives().deleteAll(inputBatch.getDeleteObjectives());

    final Uni<GrimBatchForOne> del_assignements_uni = Execute.apply(tx, del_assignements).onItem()
        .transform(row -> successOutput(inputBatch, "Assignments deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete assignments \r\n" + inputBatch.getDeleteAssignments(), e));

    final Uni<GrimBatchForOne> del_links_uni = Execute.apply(tx, del_links).onItem()
        .transform(row -> successOutput(inputBatch, "Mission links deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete mission links \r\n" + inputBatch.getDeleteLinks(), e));

    final Uni<GrimBatchForOne> del_missionLabels_uni = Execute.apply(tx, del_missionLabels).onItem()
        .transform(row -> successOutput(inputBatch, "Mission labels deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete mission labels \r\n" + inputBatch.getDeleteMissionLabels(), e));

    final Uni<GrimBatchForOne> del_data_uni = Execute.apply(tx, del_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Data deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete data \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchForOne> del_remarks_uni = Execute.apply(tx, del_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete remarks \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchForOne> del_goals_uni = Execute.apply(tx, del_goals).onItem()
        .transform(row -> successOutput(inputBatch, "Goals deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete goals \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchForOne> del_objectives_uni = Execute.apply(tx, del_objectives).onItem()
        .transform(row -> successOutput(inputBatch, "Objectives deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to delete objectives \r\n" + inputBatch.getDeleteRemarks(), e));

    
    
    // UPDATE OPERATIONS
    final var upd_missionData = registry.missionData().updateAll(inputBatch.getUpdateData());
    final var upd_remarks = registry.remarks().updateAll(inputBatch.getUpdateRemarks());
    final var upd_missionGols = registry.goals().updateAll(inputBatch.getUpdateGoals());
    final var upd_missionObjectives = registry.objectives().updateAll(inputBatch.getUpdateObjectives());
    final var upd_mission = registry.missions().updateAll(inputBatch.getUpdateMissions());
    
    final Uni<GrimBatchForOne> upd_missionData_uni = Execute.apply(tx, upd_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Mission data updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to update mission data \r\n" + inputBatch.getUpdateData(), e));

    final Uni<GrimBatchForOne> upd_remarks_uni = Execute.apply(tx, upd_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to update remarks \r\n" + inputBatch.getUpdateRemarks(), e));

    final Uni<GrimBatchForOne> upd_missionGols_uni = Execute.apply(tx, upd_missionGols).onItem()
        .transform(row -> successOutput(inputBatch, "Mission goals updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to update mission goals \r\n" + inputBatch.getUpdateGoals(), e));

    final Uni<GrimBatchForOne> upd_missionObjectives_uni = Execute.apply(tx, upd_missionObjectives).onItem()
        .transform(row -> successOutput(inputBatch, "Mission objectives updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to update mission objectives \r\n" + inputBatch.getUpdateObjectives(), e));
    
    final Uni<GrimBatchForOne> upd_mission_uni = Execute.apply(tx, upd_mission).onItem()
        .transform(row -> successOutput(inputBatch, "Mission updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to update mission data \r\n" + inputBatch.getUpdateMissions(), e));
    

    
    // INSERT OPERATIONS
    final var ins_mission = registry.missions().insertAll(inputBatch.getMissions());
    final var ins_missionLabels = registry.missionLabels().insertAll(inputBatch.getMissionLabels());
    final var ins_missionLinks = registry.missionLinks().insertAll(inputBatch.getLinks());
    final var ins_remarks = registry.remarks().insertAll(inputBatch.getRemarks());
    final var ins_objectives = registry.objectives().insertAll(inputBatch.getObjectives());
    final var ins_goals = registry.goals().insertAll(inputBatch.getGoals());
    final var ins_missionData = registry.missionData().insertAll(inputBatch.getData());
    final var ins_assignments = registry.assignments().insertAll(inputBatch.getAssignments());
    
    
    final Uni<GrimBatchForOne> ins_mission_uni = Execute.apply(tx, ins_mission).onItem()
        .transform(row -> successOutput(inputBatch, "Missions inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert missions \r\n" + inputBatch.getMissions(), e));

    final Uni<GrimBatchForOne> ins_missionLabels_uni = Execute.apply(tx, ins_missionLabels).onItem()
        .transform(row -> successOutput(inputBatch, "Missions labels inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert mission labels \r\n" + inputBatch.getMissionLabels(), e));
    
    final Uni<GrimBatchForOne> ins_missionLinks_uni = Execute.apply(tx, ins_missionLinks).onItem()
        .transform(row -> successOutput(inputBatch, "Mission links inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert mission links \r\n" + inputBatch.getLinks(), e));
    
    final Uni<GrimBatchForOne> ins_remarks_uni = Execute.apply(tx, ins_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert remarks \r\n" + inputBatch.getRemarks(), e));

    final Uni<GrimBatchForOne> ins_objectives_uni = Execute.apply(tx, ins_objectives).onItem()
        .transform(row -> successOutput(inputBatch, "Objectives inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert objectives \r\n" + inputBatch.getObjectives(), e));

    final Uni<GrimBatchForOne> ins_goals_uni = Execute.apply(tx, ins_goals).onItem()
        .transform(row -> successOutput(inputBatch, "Goals inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert goals \r\n" + inputBatch.getGoals(), e));

    final Uni<GrimBatchForOne> ins_data_uni = Execute.apply(tx, ins_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Mission data inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert mission data \r\n" + inputBatch.getData(), e));

    final Uni<GrimBatchForOne> ins_assignments_uni = Execute.apply(tx, ins_assignments).onItem()
        .transform(row -> successOutput(inputBatch, "Assignment inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert assignment \r\n" + inputBatch.getAssignments(), e));

    
    // INSERT COMMIT RELATED MODEL
    final var ins_commits = registry.commits().insertAll(inputBatch.getCommits());
    final var ins_trees = registry.commitTrees().insertAll(inputBatch.getCommitTrees());
    final var ins_viewers = registry.commitViewers().insertAll(inputBatch.getCommitViewers());
    final var ins_commands = registry.commands().insertAll(inputBatch.getCommands());
    
    final Uni<GrimBatchForOne> ins_commits_uni = Execute.apply(tx, ins_commits).onItem()
        .transform(row -> successOutput(inputBatch, "Commits inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert commits \r\n" + inputBatch.getCommits(), e));

    final Uni<GrimBatchForOne> ins_trees_uni = Execute.apply(tx, ins_trees).onItem()
        .transform(row -> successOutput(inputBatch, "Commit trees inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert commit trees \r\n" + inputBatch.getCommitTrees(), e));

    final Uni<GrimBatchForOne> ins_viewers_uni = Execute.apply(tx, ins_viewers).onItem()
        .transform(row -> successOutput(inputBatch, "Commit viewers inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert commit viewers \r\n" + inputBatch.getCommitViewers(), e));

    final Uni<GrimBatchForOne> ins_commands_uni = Execute.apply(tx, ins_commands).onItem()
        .transform(row -> successOutput(inputBatch, "Commands inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to insert commands \r\n" + inputBatch.getCommitViewers(), e));
    
    return Uni.combine().all()
    		.unis(
            del_assignements_uni,
            del_links_uni,
            del_missionLabels_uni,
            del_data_uni,
            del_remarks_uni,
            del_goals_uni,
            del_objectives_uni,

    		    ins_commits_uni,
            ins_trees_uni,
    		    
            upd_mission_uni,
            upd_missionObjectives_uni,
            upd_missionGols_uni,
            upd_missionData_uni,
    		    upd_remarks_uni,

    		    ins_mission_uni,
            ins_objectives_uni,
            ins_goals_uni,
    		    ins_missionLabels_uni,
    		    ins_missionLinks_uni,
    		    ins_remarks_uni,
    		    ins_data_uni,
    		    ins_assignments_uni,
    		    
            ins_viewers_uni,
            ins_commands_uni
    		 )
    		
    		.with(GrimBatchForOne.class, (List<GrimBatchForOne> items) -> merge(inputBatch, items));
  }

  
  private GrimBatchForOne merge(GrimBatchForOne start, List<GrimBatchForOne> current) {
    final var builder = ImmutableGrimBatchForOne.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getStatus();
    for(GrimBatchForOne value : current) {
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
  private GrimBatchForOne successOutput(GrimBatchForOne current, String msg) {
    return ImmutableGrimBatchForOne.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private GrimBatchForOne failOutput(GrimBatchForOne current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableGrimBatchForOne.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build(); 
  }
}
