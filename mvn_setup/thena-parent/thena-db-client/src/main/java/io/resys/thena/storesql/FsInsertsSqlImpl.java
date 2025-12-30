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

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.FsRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.registry.fs.FsRegistrySqlImpl;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.fs.FsInserts;
import io.resys.thena.structures.fs.ImmutableFsBatchDirents;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;


public class FsInsertsSqlImpl implements FsInserts {
  private final ThenaSqlDataSource wrapper;
  private final FsRegistry registry;
  
  public FsInsertsSqlImpl(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = new FsRegistrySqlImpl(dataSource.getRegistry());
  }
  @Override
  public Uni<FsBatchDirents> batchMany(FsBatchDirents inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();

    // DELETE OPERATIONS
    final var del_assignements = registry.direntAssignments().deleteAll(inputBatch.getDeleteAssignments());
    final var del_links = registry.direntLinks().deleteAll(inputBatch.getDeleteLinks());
    final var del_labels = registry.direntLabels().deleteAll(inputBatch.getDeleteDirentLabels());
    final var del_data = registry.direntData().deleteAll(inputBatch.getDeleteData());
    final var del_remarks = registry.direntRemarks().deleteAll(inputBatch.getDeleteRemarks());


    final Uni<FsBatchDirents> del_assignements_uni = Execute.apply(tx, del_assignements).onItem()
        .transform(row -> successOutput(inputBatch, "Assignments deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete assignments \r\n" + inputBatch.getDeleteAssignments(), e));

    final Uni<FsBatchDirents> del_links_uni = Execute.apply(tx, del_links).onItem()
        .transform(row -> successOutput(inputBatch, "Links deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete links \r\n" + inputBatch.getDeleteLinks(), e));

    final Uni<FsBatchDirents> del_labels_uni = Execute.apply(tx, del_labels).onItem()
        .transform(row -> successOutput(inputBatch, "Labels deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete labels \r\n" + inputBatch.getDeleteDirentLabels(), e));

    final Uni<FsBatchDirents> del_data_uni = Execute.apply(tx, del_data).onItem()
        .transform(row -> successOutput(inputBatch, "Data deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete data \r\n" + inputBatch.getDeleteRemarks(), e));

    final Uni<FsBatchDirents> del_remarks_uni = Execute.apply(tx, del_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete remarks \r\n" + inputBatch.getDeleteRemarks(), e));

    
    // UPDATE OPERATIONS
    final var upd_data = registry.direntData().updateAll(inputBatch.getUpdateData());
    final var upd_remarks = registry.direntRemarks().updateAll(inputBatch.getUpdateRemarks());

    final var upd_dirents = registry.dirents().updateAll(inputBatch.getUpdateDirents());
    final var upd_links = registry.direntLinks().updateAll(inputBatch.getUpdateLinks());
    
    final Uni<FsBatchDirents> upd_data_uni = Execute.apply(tx, upd_data).onItem()
        .transform(row -> successOutput(inputBatch, "Data updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update data \r\n" + inputBatch.getUpdateData(), e));

    final Uni<FsBatchDirents> upd_remarks_uni = Execute.apply(tx, upd_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update remarks \r\n" + inputBatch.getUpdateRemarks(), e));


    final Uni<FsBatchDirents> upd_dirents_uni = Execute.apply(tx, upd_dirents).onItem()
        .transform(row -> successOutput(inputBatch, "Dirents updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update dirents \r\n" + inputBatch.getUpdateDirents(), e));
    
    final Uni<FsBatchDirents> upd_links_uni = Execute.apply(tx, upd_links).onItem()
        .transform(row -> successOutput(inputBatch, "Links updated, number of updated entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to update links \r\n" + inputBatch.getUpdateLinks(), e));
    

    
    // INSERT OPERATIONS
    final var ins_dirents = registry.dirents().insertAll(inputBatch.getDirents());
    final var ins_labels = registry.direntLabels().insertAll(inputBatch.getLabels());
    final var ins_links = registry.direntLinks().insertAll(inputBatch.getLinks());
    final var ins_remarks = registry.direntRemarks().insertAll(inputBatch.getRemarks());
    final var ins_data = registry.direntData().insertAll(inputBatch.getData());
    final var ins_assignments = registry.direntAssignments().insertAll(inputBatch.getAssignments());
    
    
    final Uni<FsBatchDirents> ins_dirents_uni = Execute.apply(tx, ins_dirents).onItem()
        .transform(row -> successOutput(inputBatch, "Dirents inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert dirents \r\n" + inputBatch.getDirents(), e));

    final Uni<FsBatchDirents> ins_labels_uni = Execute.apply(tx, ins_labels).onItem()
        .transform(row -> successOutput(inputBatch, "Labels inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert labels \r\n" + inputBatch.getLabels(), e));
    
    final Uni<FsBatchDirents> ins_links_uni = Execute.apply(tx, ins_links).onItem()
        .transform(row -> successOutput(inputBatch, "Links inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert links \r\n" + inputBatch.getLinks(), e));
    
    final Uni<FsBatchDirents> ins_remarks_uni = Execute.apply(tx, ins_remarks).onItem()
        .transform(row -> successOutput(inputBatch, "Remarks inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert remarks \r\n" + inputBatch.getRemarks(), e));

    final Uni<FsBatchDirents> ins_data_uni = Execute.apply(tx, ins_data).onItem()
        .transform(row -> successOutput(inputBatch, "Data inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert data \r\n" + inputBatch.getData(), e));

    final Uni<FsBatchDirents> ins_assignments_uni = Execute.apply(tx, ins_assignments).onItem()
        .transform(row -> successOutput(inputBatch, "Assignment inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert assignment \r\n" + inputBatch.getAssignments(), e));

    
    // INSERT COMMIT RELATED MODEL
    final var ins_commits = registry.commits().insertAll(inputBatch.getCommits());
    final var ins_trees = registry.commitTrees().insertAll(inputBatch.getCommitTrees());

    
    final Uni<FsBatchDirents> ins_commits_uni = Execute.apply(tx, ins_commits).onItem()
        .transform(row -> successOutput(inputBatch, "Commits inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commits \r\n" + inputBatch.getCommits(), e));

    final Uni<FsBatchDirents> ins_trees_uni = Execute.apply(tx, ins_trees).onItem()
        .transform(row -> successOutput(inputBatch, "Commit trees inserted, number of inserted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to insert commit trees \r\n" + inputBatch.getCommitTrees(), e));

    return Uni.combine().all()
    		.unis(
            del_assignements_uni,
            del_links_uni,
            del_labels_uni,
            del_data_uni,
            del_remarks_uni,

    		    ins_commits_uni,
            ins_trees_uni,
    		    
            upd_dirents_uni,
            upd_data_uni,
    		    upd_remarks_uni,
    		    upd_links_uni,
    		    
    		    ins_dirents_uni,
    		    ins_labels_uni,
    		    ins_links_uni,
    		    ins_remarks_uni,
    		    ins_data_uni,
    		    ins_assignments_uni
    		 )
    		.with(FsBatchDirents.class, (List<FsBatchDirents> items) -> merge(inputBatch, items))
    		.onFailure(FsDirentBatchException.class)
    		.recoverWithUni((ex) -> {
    		  final var batchError = (FsDirentBatchException) ex;
    		  return tx.rollback().onItem().transform(junk -> batchError.getBatch());
    		})
    		;
  }

  
  private FsBatchDirents merge(FsBatchDirents start, List<FsBatchDirents> current) {
    final var builder = ImmutableFsBatchDirents.builder().from(start);
    final var log = new StringBuilder(start.getLog());
    var status = start.getStatus();
    for(FsBatchDirents value : current) {
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
  private FsBatchDirents successOutput(FsBatchDirents current, String msg) {
    return ImmutableFsBatchDirents.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private FsDirentBatchException failOutput(FsBatchDirents current, String msg, Throwable t) {
    return new FsDirentBatchException(ImmutableFsBatchDirents.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  public static class FsDirentBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final FsBatchDirents batch;
    
    public FsDirentBatchException(FsBatchDirents batch) {
      this.batch = batch;
    }
    public FsBatchDirents getBatch() {
      return batch;
    }
  }
  
  
}
