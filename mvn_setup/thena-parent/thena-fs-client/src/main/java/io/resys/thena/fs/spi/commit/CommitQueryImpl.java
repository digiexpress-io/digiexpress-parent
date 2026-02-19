package io.resys.thena.fs.spi.commit;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitQuery;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.api.trees.PathExpressionBuilder;
import io.resys.thena.fs.spi.branch.BranchConstants;
import io.resys.thena.fs.spi.committree.CommitTree;
import io.resys.thena.fs.spi.committree.CommitTreeBuilder;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.ImmutableCommitHistoryFilter;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitQueryImpl implements CommitQuery {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  private String branchName = BranchConstants.DEFAULT_BRANCH;
  private String fileOrFolderId;
  private boolean excludeBlobs = true;
  
  private Consumer<NameExpressionBuilder> nameExpr;
  private Consumer<PathExpressionBuilder> pathExpr;
  
  @Override
  public CommitQuery branchName(String branchName) {
    this.branchName = RepoAssert.notEmpty(branchName, () -> "branchName can't be empty!");
    return this;
  }
  @Override
  public CommitQuery fileOrFolderId(String fileOrFolderId) {
    this.fileOrFolderId = RepoAssert.notEmpty(fileOrFolderId, () -> "fileOrFolderId can't be empty!");
    return this;
  }
  @Override
  public CommitQuery path(Consumer<PathExpressionBuilder> pathExpr) {
    this.pathExpr = RepoAssert.notNull(pathExpr, () -> "pathExpr can't be empty!");
    return this;
  }
  @Override
  public CommitQuery name(Consumer<NameExpressionBuilder> nameExpr) {
    this.nameExpr = RepoAssert.notNull(nameExpr, () -> "nameExpr can't be empty!");
    return this;
  }
  @Override
  public CommitQuery excludeBlobs(boolean excludeBlobs) {
    this.excludeBlobs = excludeBlobs;
    return this;
  }
  @Override
  public Uni<Optional<CommitsByObject>> findOne() {
    return baseline().collect().asList().onItem().transformToUni(found -> {
      if(found.size() == 1) {
        return Uni.createFrom().item(found.getFirst()).map(Optional::of);
      }
      if(found.size() == 0) {
        return Uni.createFrom().item(Optional.empty());
      }
      throw new CommitQueryException(
          "Expecting 0..1 node but found " + found.size() + "" + "!", 
          JsonObject.of(
              "tenantId", tenantId,
              "fileOrFolderId", fileOrFolderId,
              "branchName", branchName, 
              "isNameExpr", nameExpr != null,
              "isPathExpr", pathExpr != null
          ));
    });
  }
  @Override
  public Uni<CommitsByObject> getOne() {
    return baseline().collect().asList().map(found -> {
      if(found.size() == 1) {
        return found.getFirst();
      }
      throw new CommitQueryException(
          "Expecting 1 node but found " + found.size() + "" + "!", 
          JsonObject.of(
              "tenantId", tenantId,
              "fileOrFolderId", fileOrFolderId,
              "branchName", branchName, 
              "isNameExpr", nameExpr != null,
              "isPathExpr", pathExpr != null
          ));
    });
  }
  @Override
  public Multi<CommitsByObject> findAll() {
    return baseline();
  }

  private Multi<CommitsByObject> join(CommitTree tree) {
    if(this.excludeBlobs) {
      return Multi.createFrom().items(tree.groupByObject().stream());  
    }
    
    final var deps = tree.getDeps();
    final var propsIds = deps.getProps().toArray(new String[] {});
    final var blobIds = deps.getBlobs().toArray(new String[] {});
    
    return Uni.combine().all().unis(
        db_uni.onItem().transformToUni(tx -> tx.query().queryBlob().findAllById(blobIds)), 
        db_uni.onItem().transformToUni(tx -> tx.query().queryProps().findAllById(propsIds))
      ).asTuple()
      .onItem().transform(tuple -> {
        
        return tree
          .addAllBlobs(tuple.getItem1())
          .addAllProps(tuple.getItem2())
          .groupByObject();
      })
      .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }
  
  
  private Multi<CommitsByObject> baseline() {
    final var filter = ImmutableCommitHistoryFilter.builder()
        .branchName(branchName)
        .build();
    return db_uni.onItem().transformToMulti(tx -> tx.query().queryCommit().findCommitHistoryByNodes(filter))
      .collect().asList()
      .map(rows -> new CommitTreeBuilder(rows).build())
      .onItem().transformToMulti(this::join);
  }
}
