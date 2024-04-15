package io.resys.thena.storesql;



import java.util.List;

import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.api.registry.OrgRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.support.Execute;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.org.ImmutableOrgBatchForOne;
import io.resys.thena.structures.org.OrgInserts;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OrgDbInsertsSqlPool implements OrgInserts {
  private final ThenaSqlDataSource wrapper;
  private final OrgRegistry registry;
  
  public OrgDbInsertsSqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().org();
  }
  @Override
  public Uni<OrgBatchForOne> batchMany(OrgBatchForOne inputBatch) {
    RepoAssert.isTrue(this.wrapper.getTx().isPresent(), () -> "Transaction must be started!");
    final var tx = wrapper.getClient();

    final var memberInsert = registry.orgMembers().insertAll(inputBatch.getMembers());
    final var membersUpdate = registry.orgMembers().updateMany(inputBatch.getMembersToUpdate());
    
    final var partiesInsert = registry.orgParties().insertAll(inputBatch.getParties());
    final var partiesUpdate = registry.orgParties().updateMany(inputBatch.getPartiesToUpdate());
    final var membershipsInsert = registry.orgMemberships().insertAll(inputBatch.getMemberships());
    
    
    final var rightsInsert = registry.orgRights().insertAll(inputBatch.getRights());
    final var rightsUpdate = registry.orgRights().updateMany(inputBatch.getRightsToUpdate());
    final var memberRightsInsert = registry.orgMemberRights().insertAll(inputBatch.getMemberRights());
    final var partyRightsInsert = registry.orgPartyRights().insertAll(inputBatch.getPartyRights());
    
    
    final var statusInsert = registry.orgActorStatus().insertAll(inputBatch.getActorStatus());
    final var statusUpdate = registry.orgActorStatus().updateMany(inputBatch.getActorStatusToUpdate());
    
    
    final var commitInsert = registry.orgCommits().insertOne(inputBatch.getCommit());
    final var treeInsert = registry.orgCommitTrees().insertAll(inputBatch.getCommitTrees());
    

    final var partyRightsDelete = registry.orgPartyRights().deleteAll(inputBatch.getPartyRightToDelete());
    final var memberRightsDelete = registry.orgMemberRights().deleteAll(inputBatch.getMemberRightsToDelete());
    final var actorStatusDelete = registry.orgActorStatus().deleteAll(inputBatch.getStatusToDelete());
    final var membershipsDelete = registry.orgMemberships().deleteAll(inputBatch.getMembershipsToDelete());

    
    final Uni<OrgBatchForOne> partyRightsDeleteUni = Execute.apply(tx, partyRightsDelete).onItem()
        .transform(row -> successOutput(inputBatch, "Party rights deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete party rights \r\n" + inputBatch.getMembers(), e));
    final Uni<OrgBatchForOne> memberRightsDeleteUni = Execute.apply(tx, memberRightsDelete).onItem()
        .transform(row -> successOutput(inputBatch, "Member rights deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete member rights \r\n" + inputBatch.getMembers(), e));
    final Uni<OrgBatchForOne> actorStatusDeleteUni = Execute.apply(tx, actorStatusDelete).onItem()
        .transform(row -> successOutput(inputBatch, "Actor status deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete actor status \r\n" + inputBatch.getMembers(), e));
    final Uni<OrgBatchForOne> membershipsDeleteUni = Execute.apply(tx, membershipsDelete).onItem()
        .transform(row -> successOutput(inputBatch, "Memberships deleted, number of deleted entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to delete memberships \r\n" + inputBatch.getMembers(), e));
    
    
    // Member insert/update
    final Uni<OrgBatchForOne> memberInsertUni = Execute.apply(tx, memberInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Members saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save members \r\n" + inputBatch.getMembers(), e));
    final Uni<OrgBatchForOne> memberUpdateUni = Execute.apply(tx, membersUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Members saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to change members \r\n" + inputBatch.getMembers(), e));

    
    // Parties insert/update
    final Uni<OrgBatchForOne> partiesInsertUni = Execute.apply(tx, partiesInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Parties saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save parties \r\n" + inputBatch.getParties(), e));
    final Uni<OrgBatchForOne> partiesUpdateUni = Execute.apply(tx, partiesUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Parties saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to change parties \r\n" + inputBatch.getParties(), e));
    
    final Uni<OrgBatchForOne> membershipUni = Execute.apply(tx, membershipsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Memberships saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save memberships \r\n" + inputBatch.getMemberships(), e));
    
    // Role related
    final Uni<OrgBatchForOne> rightsInsertUni = Execute.apply(tx, rightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save rights \r\n" + inputBatch.getRights(), e));
    final Uni<OrgBatchForOne> rightsUpdateUni = Execute.apply(tx, rightsUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Rights saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to change rights \r\n" + inputBatch.getRights(), e));
    final Uni<OrgBatchForOne> partyRightsUni = Execute.apply(tx, partyRightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Parties rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save group rights \r\n" + inputBatch.getPartyRights(), e));
    final Uni<OrgBatchForOne> memberRightsUni = Execute.apply(tx, memberRightsInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Member rights saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save rights \r\n" + inputBatch.getMemberRights(), e));
    
    // Status related
    final Uni<OrgBatchForOne> statusInsertUni = Execute.apply(tx, statusInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of new entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save status \r\n" + inputBatch.getParties(), e));
    final Uni<OrgBatchForOne> statusUpdateUni = Execute.apply(tx, statusUpdate).onItem()
        .transform(row -> successOutput(inputBatch, "Status saved, number of changed entries: " + + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to change status \r\n" + inputBatch.getParties(), e));
    
    
    // Commit log
    final Uni<OrgBatchForOne> commitUni = Execute.apply(tx, commitInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save commit", e));
    
    final Uni<OrgBatchForOne> treeUni = Execute.apply(tx, treeInsert).onItem()
        .transform(row -> successOutput(inputBatch, "Commit tree saved, number of new entries: " + (row == null ? 0 : row.rowCount())))
        .onFailure().transform(e -> failOutput(inputBatch, "Failed to save commit tree", e));
    
    // combine all
    return Uni.combine().all()
    		.unis(
    		    partyRightsDeleteUni,
    		    memberRightsDeleteUni,
    		    actorStatusDeleteUni,
    		    membershipsDeleteUni,
    		    
    		    commitUni,
    		    memberInsertUni, memberUpdateUni, 
    		    partiesInsertUni, partiesUpdateUni, membershipUni, 
    		    rightsInsertUni, rightsUpdateUni, partyRightsUni, memberRightsUni,
    		    statusInsertUni, statusUpdateUni,
    		    treeUni
    		 )
    		.with(OrgBatchForOne.class, (List<OrgBatchForOne> items) -> merge(inputBatch, items))
    		.onFailure(OrgBatchException.class)
        .recoverWithUni((ex) -> {
          final var batchError = (OrgBatchException) ex;
          return tx.rollback().onItem().transform(junk -> batchError.getBatch());
        });
  }

  
  private OrgBatchForOne merge(OrgBatchForOne start, List<OrgBatchForOne> current) {
    final var builder = ImmutableOrgBatchForOne.builder().from(start);
    final var log = new StringBuilder(start.getLog());
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
  private OrgBatchForOne successOutput(OrgBatchForOne current, String msg) {
    return ImmutableOrgBatchForOne.builder()
      .from(current)
      .status(BatchStatus.OK)
      .addMessages(ImmutableMessage.builder().text(msg).build())
      .build();
  }
  
  private OrgBatchException failOutput(OrgBatchForOne current, String msg, Throwable t) {
    log.error("Batch failed because of: " + msg, t);
    return new OrgBatchException(ImmutableOrgBatchForOne.builder()
        .from(current)
        .status(BatchStatus.ERROR)
        .addMessages(ImmutableMessage.builder().text(msg).exception(t).build())
        .addMessages(ImmutableMessage.builder().text(t.getMessage()).build())
        .build()); 
  }
  
  public static class OrgBatchException extends RuntimeException {
    private static final long serialVersionUID = -7251738425609399151L;
    private final ImmutableOrgBatchForOne batch;
    
    public OrgBatchException(ImmutableOrgBatchForOne batch) {
      this.batch = batch;
    }
    public ImmutableOrgBatchForOne getBatch() {
      return batch;
    }
  } 
}
