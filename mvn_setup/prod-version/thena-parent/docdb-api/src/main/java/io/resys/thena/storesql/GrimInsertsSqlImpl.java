package io.resys.thena.storesql;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import java.util.List;

import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.GrimRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts;
import io.resys.thena.structures.grim.ImmutableGrimBatchForViewers;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;


public class GrimInsertsSqlImpl implements GrimInserts {
  private final ThenaSqlDataSource wrapper;
  private final GrimRegistry registry;
  
  public GrimInsertsSqlImpl(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().grim();
  }

  @Override
  public Uni<GrimBatchForViewers> batchMany(GrimBatchForViewers inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    final var ins_viewers = registry.commitViewers().insertAll(inputBatch.getViewers());
    final var upd_viewers = registry.commitViewers().updateAll(inputBatch.getUpdateViewers());
    
    final Uni<GrimBatchForViewers> ins_viewers_uni = Execute.apply(tx, ins_viewers).onItem()
        .transform(row -> successOutput(inputBatch, "Viewers inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert viewers \r\n" + inputBatch.getViewers(), e));

    
    final Uni<GrimBatchForViewers> upd_viewers_uni = Execute.apply(tx, upd_viewers).onItem()
        .transform(row -> successOutput(inputBatch, "Viewers updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update viewers \r\n" + inputBatch.getUpdateViewers(), e));

    
    return Uni.combine().all()
        .unis(ins_viewers_uni, upd_viewers_uni)
        .with(GrimBatchForViewers.class, (List<GrimBatchForViewers> items) -> merge(inputBatch, items))
        .onFailure(GrimViewerBatchException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (GrimViewerBatchException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        });
  }   
  @Override
  public Uni<GrimBatchMissions> batchMany(GrimBatchMissions inputBatch) {
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

    final Uni<GrimBatchMissions> del_assignements_uni = Execute.apply(tx, del_assignements).onItem()
        .transform(row -> successOutput(inputBatch, "Assignments deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete assignments \r\n" + inputBatch.getDeleteAssignments(), e));

    final Uni<GrimBatchMissions> del_links_uni = Execute.apply(tx, del_links).onItem()
        .transform(row -> successOutput(inputBatch, "Mission links deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete mission links \r\n" + inputBatch.getDeleteLinks(), e));

    final Uni<GrimBatchMissions> del_missionLabels_uni = Execute.apply(tx, del_missionLabels).onItem()
        .transform(row -> successOutput(inputBatch, "Mission labels deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete mission labels \r\n" + inputBatch.getDeleteMissionLabels(), e));

    final Uni<GrimBatchMissions> del_data_uni = Execute.apply(tx, del_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Data deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete data \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchMissions> del_remarks_uni = Execute.apply(tx, del_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete remarks \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchMissions> del_goals_uni = Execute.apply(tx, del_goals).onItem()
        .transform(row -> successOutput(inputBatch, "Goals deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete goals \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<GrimBatchMissions> del_objectives_uni = Execute.apply(tx, del_objectives).onItem()
        .transform(row -> successOutput(inputBatch, "Objectives deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete objectives \r\n" + inputBatch.getDeleteRemarks(), e));

    
    
    // UPDATE OPERATIONS
    final var upd_missionData = registry.missionData().updateAll(inputBatch.getUpdateData());
    final var upd_remarks = registry.remarks().updateAll(inputBatch.getUpdateRemarks());
    final var upd_missionGols = registry.goals().updateAll(inputBatch.getUpdateGoals());
    final var upd_missionObjectives = registry.objectives().updateAll(inputBatch.getUpdateObjectives());
    final var upd_mission = registry.missions().updateAll(inputBatch.getUpdateMissions());
    final var upd_links = registry.missionLinks().updateAll(inputBatch.getUpdateLinks());
    
    final Uni<GrimBatchMissions> upd_missionData_uni = Execute.apply(tx, upd_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Mission data updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update mission data \r\n" + inputBatch.getUpdateData(), e));

    final Uni<GrimBatchMissions> upd_remarks_uni = Execute.apply(tx, upd_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update remarks \r\n" + inputBatch.getUpdateRemarks(), e));

    final Uni<GrimBatchMissions> upd_missionGols_uni = Execute.apply(tx, upd_missionGols).onItem()
        .transform(row -> successOutput(inputBatch, "Mission goals updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update mission goals \r\n" + inputBatch.getUpdateGoals(), e));

    final Uni<GrimBatchMissions> upd_missionObjectives_uni = Execute.apply(tx, upd_missionObjectives).onItem()
        .transform(row -> successOutput(inputBatch, "Mission objectives updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update mission objectives \r\n" + inputBatch.getUpdateObjectives(), e));
    
    final Uni<GrimBatchMissions> upd_mission_uni = Execute.apply(tx, upd_mission).onItem()
        .transform(row -> successOutput(inputBatch, "Missions updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update missions \r\n" + inputBatch.getUpdateMissions(), e));
    
    final Uni<GrimBatchMissions> upd_links_uni = Execute.apply(tx, upd_links).onItem()
        .transform(row -> successOutput(inputBatch, "Mission links updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update mission links \r\n" + inputBatch.getUpdateMissions(), e));
    

    
    // INSERT OPERATIONS
    final var ins_mission = registry.missions().insertAll(inputBatch.getMissions());
    final var ins_missionLabels = registry.missionLabels().insertAll(inputBatch.getMissionLabels());
    final var ins_missionLinks = registry.missionLinks().insertAll(inputBatch.getLinks());
    final var ins_remarks = registry.remarks().insertAll(inputBatch.getRemarks());
    final var ins_objectives = registry.objectives().insertAll(inputBatch.getObjectives());
    final var ins_goals = registry.goals().insertAll(inputBatch.getGoals());
    final var ins_missionData = registry.missionData().insertAll(inputBatch.getData());
    final var ins_assignments = registry.assignments().insertAll(inputBatch.getAssignments());
    
    
    final Uni<GrimBatchMissions> ins_mission_uni = Execute.apply(tx, ins_mission).onItem()
        .transform(row -> successOutput(inputBatch, "Missions inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert missions \r\n" + inputBatch.getMissions(), e));

    final Uni<GrimBatchMissions> ins_missionLabels_uni = Execute.apply(tx, ins_missionLabels).onItem()
        .transform(row -> successOutput(inputBatch, "Missions labels inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert mission labels \r\n" + inputBatch.getMissionLabels(), e));
    
    final Uni<GrimBatchMissions> ins_missionLinks_uni = Execute.apply(tx, ins_missionLinks).onItem()
        .transform(row -> successOutput(inputBatch, "Mission links inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert mission links \r\n" + inputBatch.getLinks(), e));
    
    final Uni<GrimBatchMissions> ins_remarks_uni = Execute.apply(tx, ins_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert remarks \r\n" + inputBatch.getRemarks(), e));

    final Uni<GrimBatchMissions> ins_objectives_uni = Execute.apply(tx, ins_objectives).onItem()
        .transform(row -> successOutput(inputBatch, "Objectives inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert objectives \r\n" + inputBatch.getObjectives(), e));

    final Uni<GrimBatchMissions> ins_goals_uni = Execute.apply(tx, ins_goals).onItem()
        .transform(row -> successOutput(inputBatch, "Goals inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert goals \r\n" + inputBatch.getGoals(), e));

    final Uni<GrimBatchMissions> ins_data_uni = Execute.apply(tx, ins_missionData).onItem()
        .transform(row -> successOutput(inputBatch, "Mission data inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert mission data \r\n" + inputBatch.getData(), e));

    final Uni<GrimBatchMissions> ins_assignments_uni = Execute.apply(tx, ins_assignments).onItem()
        .transform(row -> successOutput(inputBatch, "Assignment inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert assignment \r\n" + inputBatch.getAssignments(), e));

    
    // INSERT COMMIT RELATED MODEL
    final var ins_commits = registry.commits().insertAll(inputBatch.getCommits());
    final var ins_trees = registry.commitTrees().insertAll(inputBatch.getCommitTrees());
    final var ins_viewers = registry.commitViewers().insertAll(inputBatch.getCommitViewers());
    final var ins_commands = registry.commands().insertAll(inputBatch.getCommands());
    
    final Uni<GrimBatchMissions> ins_commits_uni = Execute.apply(tx, ins_commits).onItem()
        .transform(row -> successOutput(inputBatch, "Commits inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commits \r\n" + inputBatch.getCommits(), e));

    final Uni<GrimBatchMissions> ins_trees_uni = Execute.apply(tx, ins_trees).onItem()
        .transform(row -> successOutput(inputBatch, "Commit trees inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commit trees \r\n" + inputBatch.getCommitTrees(), e));

    final Uni<GrimBatchMissions> ins_viewers_uni = Execute.apply(tx, ins_viewers).onItem()
        .transform(row -> successOutput(inputBatch, "Commit viewers inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commit viewers \r\n" + inputBatch.getCommitViewers(), e));

    final Uni<GrimBatchMissions> ins_commands_uni = Execute.apply(tx, ins_commands).onItem()
        .transform(row -> successOutput(inputBatch, "Commands inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commands \r\n" + inputBatch.getCommands(), e));
    
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
    		    upd_links_uni,
    		    
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
    		.with(GrimBatchMissions.class, (List<GrimBatchMissions> items) -> merge(inputBatch, items))
    		.onFailure(GrimMissionBatchException.class)
    		.recoverWithUni((ex) -> {
    		  final var batchError = (GrimMissionBatchException) ex;
    		  return tx.rollback().onItem().transform(junk -> batchError.getBatch());
    		})
    		;
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
  private GrimBatchMissions successOutput(GrimBatchMissions current, String msg) {
    return ImmutableGrimBatchMissions.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private GrimMissionBatchException failOutput(GrimBatchMissions current, String msg, Throwable t) {
    return new GrimMissionBatchException(ImmutableGrimBatchMissions.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  public static class GrimMissionBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final GrimBatchMissions batch;
    
    public GrimMissionBatchException(GrimBatchMissions batch) {
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
  
  
  private GrimBatchForViewers merge(GrimBatchForViewers start, List<GrimBatchForViewers> current) {
    final var builder = ImmutableGrimBatchForViewers.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getStatus();
    for(GrimBatchForViewers value : current) {
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
  
  private GrimBatchForViewers successOutput(GrimBatchForViewers current, String msg) {
    return ImmutableGrimBatchForViewers.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private GrimViewerBatchException failOutput(GrimBatchForViewers current, String msg, Throwable t) {
    return new GrimViewerBatchException(ImmutableGrimBatchForViewers.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  public static class GrimViewerBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final GrimBatchForViewers batch;
    
    public GrimViewerBatchException(GrimBatchForViewers batch) {
      this.batch = batch;
    }
    public GrimBatchForViewers getBatch() {
      return batch;
    }
  }
}
