package io.resys.thena.docdb.store.sql.queries;



import java.util.List;
import java.util.function.Predicate;

import io.resys.thena.docdb.api.models.ImmutableMessage;
import io.resys.thena.docdb.api.models.ThenaOrgObject.IsOrgObject;
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
  
  @RequiredArgsConstructor
  private static class IsInsert implements Predicate<IsOrgObject>  {
    private final OrgBatchForOne inputBatch;
    @Override
    public boolean test(IsOrgObject t) {
      return !inputBatch.getIdentifiersForUpdates().contains(t.getId());
    }
  }
  @RequiredArgsConstructor
  private static class IsUpdate implements Predicate<IsOrgObject>  {
    private final OrgBatchForOne inputBatch;
    @Override
    public boolean test(IsOrgObject t) {
      return inputBatch.getIdentifiersForUpdates().contains(t.getId());
    }
  }
  
  @Override
  public Uni<OrgBatchForOne> batchOne(OrgBatchForOne inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    final var isInsert = new IsInsert(inputBatch);
    final var isUpdate = new IsUpdate(inputBatch);

    final var usersInsert = sqlBuilder.orgUsers().insertAll(inputBatch.getUsers().stream().filter(isInsert).toList());
    final var usersUpdate = sqlBuilder.orgUsers().updateMany(inputBatch.getUsers().stream().filter(isUpdate).toList());
    
    final var groupsInsert = sqlBuilder.orgGroups().insertAll(inputBatch.getGroups().stream().filter(isInsert).toList());
    final var groupsUpdate = sqlBuilder.orgGroups().updateMany(inputBatch.getGroups().stream().filter(isUpdate).toList());
    final var userMembershipsInsert = sqlBuilder.orgUserMemberships().insertAll(inputBatch.getUserMemberships());
    
    
    final var rolesInsert = sqlBuilder.orgRoles().insertAll(inputBatch.getRoles().stream().filter(isInsert).toList());
    final var rolesUpdate = sqlBuilder.orgRoles().updateMany(inputBatch.getRoles().stream().filter(isUpdate).toList());
    final var userRolesInsert = sqlBuilder.orgUserRoles().insertAll(inputBatch.getUserRoles());
    final var groupRolesInsert = sqlBuilder.orgGroupRoles().insertAll(inputBatch.getGroupRoles());
    
    
    final var statusInsert = sqlBuilder.orgActorStatus().insertAll(inputBatch.getActorStatus().stream().filter(isInsert).toList());
    final var statusUpdate = sqlBuilder.orgActorStatus().updateMany(inputBatch.getActorStatus().stream().filter(isUpdate).toList());
    
    
    final var commitInsert = sqlBuilder.orgCommits().insertOne(inputBatch.getCommit());
    final var treeInsert = sqlBuilder.orgCommitTrees().insertAll(inputBatch.getCommit().getTree());
    
    
    // User insert/update
    final Uni<OrgBatchForOne> userInsertUni = Execute.apply(tx, usersInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Users saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save users \r\n" + inputBatch.getUsers(), e));
    final Uni<OrgBatchForOne> userUpdateUni = Execute.apply(tx, usersUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Users saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change users \r\n" + inputBatch.getUsers(), e));

    
    // Group insert/update
    final Uni<OrgBatchForOne> groupsInsertUni = Execute.apply(tx, groupsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Groups saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save groups \r\n" + inputBatch.getGroups(), e));
    final Uni<OrgBatchForOne> groupsUpdateUni = Execute.apply(tx, groupsUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Groups saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change groups \r\n" + inputBatch.getGroups(), e));
    
    final Uni<OrgBatchForOne> membershipUni = Execute.apply(tx, userMembershipsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "User memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save User memberships \r\n" + inputBatch.getUserMemberships(), e));
    
    // Role related
    final Uni<OrgBatchForOne> roleInsertUni = Execute.apply(tx, rolesInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Roles saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save roles \r\n" + inputBatch.getRoles(), e));
    final Uni<OrgBatchForOne> roleUpdateUni = Execute.apply(tx, rolesUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Roles saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change roles \r\n" + inputBatch.getRoles(), e));
    final Uni<OrgBatchForOne> groupRolesUni = Execute.apply(tx, groupRolesInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Group roles memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save group roles \r\n" + inputBatch.getGroupRoles(), e));
    final Uni<OrgBatchForOne> userRolesUni = Execute.apply(tx, userRolesInsert).onItem()
        .transform(row -> successOutput(inputBatch, "User roles memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save User roles \r\n" + inputBatch.getUserRoles(), e));
    
    // Status related
    final Uni<OrgBatchForOne> statusInsertUni = Execute.apply(tx, statusInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save status \r\n" + inputBatch.getGroups(), e));
    final Uni<OrgBatchForOne> statusUpdateUni = Execute.apply(tx, statusUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change status \r\n" + inputBatch.getGroups(), e));
    
    
    // Commit log
    final Uni<OrgBatchForOne> commitUni = Execute.apply(tx, commitInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save commit", e));
    
    final Uni<OrgBatchForOne> treeUni = Execute.apply(tx, treeInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit tree saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save commit tree", e));
    
    // combine all
    return Uni.combine().all()
    		.unis(
    		    userInsertUni, userUpdateUni, 
    		    groupsInsertUni, groupsUpdateUni, membershipUni, 
    		    roleInsertUni, roleUpdateUni, groupRolesUni, userRolesUni,
    		    statusInsertUni, statusUpdateUni,
    		    commitUni, treeUni
    		 )
    		.combinedWith(OrgBatchForOne.class, (List<OrgBatchForOne> items) -> merge(inputBatch, items));
  }
  @Override
  public Uni<OrgBatchForMany> batchMany(OrgBatchForMany output) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();
    
    
    // TODO Auto-generated method stub
    return null;
  }


  
  private OrgBatchForOne merge(OrgBatchForOne start, List<OrgBatchForOne> current) {
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
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
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
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build(); 
  }
}
