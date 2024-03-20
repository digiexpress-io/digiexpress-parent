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

    final var memberInsert = sqlBuilder.orgMembers().insertAll(inputBatch.getMembers().stream().filter(isInsert).toList());
    final var membersUpdate = sqlBuilder.orgMembers().updateMany(inputBatch.getMembers().stream().filter(isUpdate).toList());
    
    final var partiesInsert = sqlBuilder.orgParties().insertAll(inputBatch.getParties().stream().filter(isInsert).toList());
    final var partiesUpdate = sqlBuilder.orgParties().updateMany(inputBatch.getParties().stream().filter(isUpdate).toList());
    final var membershipsInsert = sqlBuilder.orgMemberships().insertAll(inputBatch.getMemberships());
    
    
    final var rightsInsert = sqlBuilder.orgRights().insertAll(inputBatch.getRights().stream().filter(isInsert).toList());
    final var rightsUpdate = sqlBuilder.orgRights().updateMany(inputBatch.getRights().stream().filter(isUpdate).toList());
    final var memberRightsInsert = sqlBuilder.orgMemberRights().insertAll(inputBatch.getMemberRights());
    final var partyRightsInsert = sqlBuilder.orgPartyRights().insertAll(inputBatch.getPartyRights());
    
    
    final var statusInsert = sqlBuilder.orgActorStatus().insertAll(inputBatch.getActorStatus().stream().filter(isInsert).toList());
    final var statusUpdate = sqlBuilder.orgActorStatus().updateMany(inputBatch.getActorStatus().stream().filter(isUpdate).toList());
    
    
    final var commitInsert = sqlBuilder.orgCommits().insertOne(inputBatch.getCommit());
    final var treeInsert = sqlBuilder.orgCommitTrees().insertAll(inputBatch.getCommit().getTree());
    
    
    // Member insert/update
    final Uni<OrgBatchForOne> memberInsertUni = Execute.apply(tx, memberInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Members saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save members \r\n" + inputBatch.getMembers(), e));
    final Uni<OrgBatchForOne> memberUpdateUni = Execute.apply(tx, membersUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Members saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change members \r\n" + inputBatch.getMembers(), e));

    
    // Parties insert/update
    final Uni<OrgBatchForOne> partiesInsertUni = Execute.apply(tx, partiesInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Parties saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save parties \r\n" + inputBatch.getParties(), e));
    final Uni<OrgBatchForOne> partiesUpdateUni = Execute.apply(tx, partiesUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Parties saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change parties \r\n" + inputBatch.getParties(), e));
    
    final Uni<OrgBatchForOne> membershipUni = Execute.apply(tx, membershipsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save memberships \r\n" + inputBatch.getMemberships(), e));
    
    // Role related
    final Uni<OrgBatchForOne> rightsInsertUni = Execute.apply(tx, rightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save rights \r\n" + inputBatch.getRights(), e));
    final Uni<OrgBatchForOne> rightsUpdateUni = Execute.apply(tx, rightsUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Rights saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change rights \r\n" + inputBatch.getRights(), e));
    final Uni<OrgBatchForOne> partyRightsUni = Execute.apply(tx, partyRightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Parties rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save group rights \r\n" + inputBatch.getPartyRights(), e));
    final Uni<OrgBatchForOne> memberRightsUni = Execute.apply(tx, memberRightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Member rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save rights \r\n" + inputBatch.getMemberRights(), e));
    
    // Status related
    final Uni<OrgBatchForOne> statusInsertUni = Execute.apply(tx, statusInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to save status \r\n" + inputBatch.getParties(), e));
    final Uni<OrgBatchForOne> statusUpdateUni = Execute.apply(tx, statusUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().recoverWithItem(e -> failOutput(inputBatch, "Failed to change status \r\n" + inputBatch.getParties(), e));
    
    
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
    		    commitUni,
    		    memberInsertUni, memberUpdateUni, 
    		    partiesInsertUni, partiesUpdateUni, membershipUni, 
    		    rightsInsertUni, rightsUpdateUni, partyRightsUni, memberRightsUni,
    		    statusInsertUni, statusUpdateUni,
    		    treeUni
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
