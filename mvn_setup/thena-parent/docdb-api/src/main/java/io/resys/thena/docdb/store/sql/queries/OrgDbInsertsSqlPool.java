package io.resys.thena.docdb.store.sql.queries;



import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.models.git.GitInserts.BatchStatus;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForMany;
import io.resys.thena.docdb.models.org.ImmutableOrgBatchForOne;
import io.resys.thena.docdb.models.org.OrgInserts;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.Execute;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class OrgDbInsertsSqlPool implements OrgInserts {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;
  
  @Override
  public Uni<OrgBatchForOne> batchOne(OrgBatchForOne inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    // user - commit - tree
    final var usersInsert = sqlBuilder.orgUsers().insertAll(inputBatch.getUsers());
    final var groupsInsert = sqlBuilder.orgGroups().insertAll(inputBatch.getGroups());
    final var userMembershipsInsert = sqlBuilder.orgUserMemberships().insertAll(inputBatch.getUserMemberships());
    final var commitInsert = sqlBuilder.orgCommits().insertOne(inputBatch.getCommit());
    final var treeInsert = sqlBuilder.orgCommitTrees().insertAll(inputBatch.getCommit().getTree());
    
    final Uni<OrgBatchForOne> userUni = Execute.apply(tx, usersInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Users saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save users \r\n" + inputBatch.getUsers(), e));

    final Uni<OrgBatchForOne> groupsUni = Execute.apply(tx, groupsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Groups saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save groups \r\n" + inputBatch.getUsers(), e));
    
    final Uni<OrgBatchForOne> membershipUni = Execute.apply(tx, userMembershipsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "User memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save User memberships \r\n" + inputBatch.getUsers(), e));
    
    final Uni<OrgBatchForOne> commitUni = Execute.apply(tx, commitInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save commit", e));
    
    final Uni<OrgBatchForOne> treeUni = Execute.apply(tx, treeInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit tree saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save commit tree", e));
    
    return Uni.combine().all().unis(userUni, groupsUni, commitUni, treeUni, membershipUni).asTuple()
        .onItem().transform(tuple -> merge(
                inputBatch,
                tuple.getItem1(), 
                tuple.getItem2(), 
                tuple.getItem3(),
                tuple.getItem4(),
                tuple.getItem5()
        ));
  }
  @Override
  public Uni<OrgBatchForMany> batchMany(OrgBatchForMany output) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    
    // TODO Auto-generated method stub
    return null;
  }


  
  private OrgBatchForOne merge(OrgBatchForOne start, OrgBatchForOne ... current) {
    final var builder = ImmutableOrgBatchForOne.builder().from(start);
    final var log = new StringBuilder(start.getLog().getText());
    var status = start.getStatus();
    for(OrgBatchForOne value : current) {
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
  private OrgBatchForMany successOutput(OrgBatchForMany current, String msg) {
    return ImmutableOrgBatchForMany.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  private OrgBatchForMany failOutput(OrgBatchForMany current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableOrgBatchForMany.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
  
  private OrgBatchForOne successOutput(OrgBatchForOne current, String msg) {
    return ImmutableOrgBatchForOne.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private OrgBatchForOne failOutput(OrgBatchForOne current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return ImmutableOrgBatchForOne.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).build())
        .build(); 
  }
}
