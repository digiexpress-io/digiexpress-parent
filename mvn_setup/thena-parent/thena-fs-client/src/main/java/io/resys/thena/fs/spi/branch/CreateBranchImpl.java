package io.resys.thena.fs.spi.branch;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.fs.api.branches.BranchBuilder;
import io.resys.thena.fs.api.branches.BranchBuilder.BeforeBranchCompletion;
import io.resys.thena.fs.api.branches.BranchResult;
import io.resys.thena.fs.api.branches.CreateBranch;
import io.resys.thena.fs.api.branches.ImmutableBranchResult;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableRefTransitives;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.resys.thena.fs.spi.commit.CommitBuilderException;
import io.resys.thena.fs.spi.commit.CommitBuilderImpl;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.FsDbBuilder.FsBuilderException;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class CreateBranchImpl implements CreateBranch {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  private String branchAuthor;
  private String commitIdOrBranchName;
  private OffsetDateTime branchCreatedAt;
  private BeforeBranchCompletion beforeBranchCompletion;
  private Consumer<BranchBuilder> branchBuilder;

  @Override
  public CreateBranch commitIdOrBranchName(String commitIdOrBranchName) {
    this.commitIdOrBranchName = RepoAssert.notEmpty(commitIdOrBranchName, () -> "commitIdOrBranchName can't be empty!");
    return this;
  }
  @Override
  public CreateBranch newBranch(Consumer<BranchBuilder> branchBuilder) {
    this.branchBuilder = RepoAssert.notNull(branchBuilder, () -> "branchBuilder can't be null!");
    return this;
  }
  @Override
  public CreateBranch branchCreatedAt(OffsetDateTime branchCreatedAt) {
    this.branchCreatedAt = RepoAssert.notNull(branchCreatedAt, () -> "branchCreatedAt can't be empty!");
    return this;
  }
  @Override
  public CreateBranch beforeBranchCompletion(BeforeBranchCompletion callback) {
    this.beforeBranchCompletion = RepoAssert.notNull(callback, () -> "beforeBranchCompletion can't be null!");
    return this;
  }
  @Override
  public CreateBranch branchAuthor(String branchAuthor) {
    this.branchAuthor = RepoAssert.notEmpty(branchAuthor, () -> "branchAuthor can't be empty!");
    return this;
  }
  @Override
  public Uni<BranchResult> build() {
    RepoAssert.notEmpty(this.branchAuthor, () -> "branchAuthor can't be empty!");
    RepoAssert.notNull(this.branchBuilder, () -> "branchBuilder can't be null!");
    final var commitIdOrBranchName = RepoAssert.notEmpty(this.commitIdOrBranchName, () -> "commitIdOrBranchName can't be empty!");
    
    
    final var scope = ImmutableTxScope.builder()
        .commitAuthor(commitIdOrBranchName)
        .commitMessage("creating new branch")
        .tenantId(tenantId)
        .build();
    return this.db_uni
        .onItem().transformToUni(db -> db.withTransaction(scope, this::visitTransaction))
        .onFailure(t -> {
          // force crash on anything
          return false;
        })
        .recoverWithItem(this::visitFailure);
  }

  private Uni<BranchResult> visitTransaction(FsDb tx) {
    return tx.query().queryCommit().findByCommitIdOrRef(commitIdOrBranchName)
        .map(found -> {
          if(found.isEmpty()) {
            throw new BranchCreationException("Can't find commit or branch by given id", JsonObject.of("id", commitIdOrBranchName));
          }
          return found.get();
        })
        .onItem().transformToUni(found -> {
          if(beforeBranchCompletion != null) {
            return tx.query().queryCommit().getByIdWithNodesAndBlobs(found.getItem1().getId())
              .map(tuple -> tuple.getItem2())
              .onItem().transform(tree -> new BranchRequest(found.getItem1(), Optional.ofNullable(tree), found.getItem2()));  
          }
          return Uni.createFrom().item(new BranchRequest(found.getItem1(), Optional.empty(), found.getItem2()));
        })
        .onItem().transformToUni(request -> visitPersistenceUnit(tx, request))
        .onItem().transform(this::visitSuccess);
  }
  
  private BranchResult visitFailure(Throwable t) {
    if(t instanceof FsBuilderException) {
      final var fs = (FsBuilderException) t;
      final var builder = ImmutableBranchResult.builder()
          .tenantId(tenantId)
          .status(CommitResultStatus.ERROR)
          .addMessages(ImmutableMessage.builder()
              .exception(fs)
              .text(fs.getMessage())
              .build());
      
      if(t.getCause() != null) {
        return builder
          .addMessages(ImmutableMessage.builder().text(fs.getCause().getMessage()).build())
          .build();
      }
      
      return builder.build();
    }
    
    return ImmutableBranchResult.builder()
        .tenantId(tenantId)
        .status(CommitResultStatus.ERROR)
        .addMessages(ImmutableMessage.builder()
            .text("Branch creation has failed with unknown exception!")
            .exception(t)
            .build())
        .build();
  }
  
  private BranchResult visitSuccess(PersistenceUnit unit) {
    return ImmutableBranchResult.builder()
      .addAllMessages(
          unit.getCommitMessages().stream()
            .map(text -> ImmutableMessage.builder().text(text).build())
            .toList()
      )
      .addAllMessages(unit.getCommitLogs())
      .tenantId(tenantId)
      .status(CommitBuilderImpl.mapCommitStatus(unit.getStatus()))
      .branch(unit.getRefInserts().isEmpty() ? null : unit.getRefInserts().getLast())
      .build();
  }

  private Uni<PersistenceUnit> visitPersistenceUnit(FsDb tx, BranchRequest request) {
    try {
      final var createdAt = branchCreatedAt != null ? branchCreatedAt : OffsetDateTime.now();
      final var prevRef = Optional.<Ref>empty();
      final var commitId = request.getLock().getId();
      final var refTransitives = ImmutableRefTransitives.builder().commit(request.getLock()).build();  
      final var createdFrom = request.getRef().map(r -> r.getId());
      
      
      final var branchBuilder = new BranchBuilderImpl(prevRef, commitId, refTransitives, createdAt, branchAuthor, createdFrom);
      this.branchBuilder.accept(branchBuilder);
      if(this.beforeBranchCompletion != null) {
        this.beforeBranchCompletion.apply(refTransitives, branchBuilder);
      }
      final var result = branchBuilder.close();
      
      final var unit = ImmutablePersistenceUnit.builder()
          .tenantId(tenantId)
          .status(BatchStatus.OK)
          .log("")
          .addRefInserts(result.getRef())
          .build();
      return tx.builder().from(unit).persist();
    } catch(Exception e) {
      throw new CommitBuilderException(e, JsonObject.of("error", e.getMessage(), "tenantId", tenantId, "message", e.getMessage()));
    }
  }
  
  @Value
  private static class BranchRequest {
    Commit lock; 
    Optional<Tree> tree;
    Optional<Ref> ref;
  }
}
