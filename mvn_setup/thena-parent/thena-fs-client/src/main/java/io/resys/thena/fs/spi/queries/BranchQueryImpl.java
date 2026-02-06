package io.resys.thena.fs.spi.queries;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.filters.ImmutableRefTableFilter;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BranchQueryImpl implements BranchQuery {

  private final Uni<FsDb> db_uni;
  private Consumer<NameExpressionBuilder> nameExpr;
  private String branchId;
  
  @Override
  public BranchQuery branchId(String branchId) {
    RepoAssert.notEmpty(branchId, () -> "branchId can't be empty!");
    this.branchId = branchId;
    return this;
  }  
  @Override
  public BranchQuery branchName(Consumer<NameExpressionBuilder> nameExpr) {
    RepoAssert.notNull(nameExpr, () -> "nameExpr can't be empty!");
    return this;
  }
  
  private Multi<Ref> baseline() {
    final var filter = ImmutableRefTableFilter.builder().branchId(branchId).nameExpr(nameExpr).build();
    return db_uni.onItem().transformToMulti(db -> db.query().queryRef().findAllByFilter(filter));
  }
  
  @Override
  public Multi<Ref> findAll() {
    return baseline();
  }

  @Override
  public Uni<Optional<Ref>> findOne() {
    return baseline().collect().asList().map(found -> {
      final var actual = found.size();
      if(actual > 1) {
        throw new FileSystemQueryException("Expecting exactly 1 or 0 result but found: " + actual + "!", 
          JsonObject.of(
            "branchId", branchId,
            "isNameExpr", nameExpr == null ? false : true 
          )
        );
      }
      
      return found.stream().findFirst();
    });
  }
  @Override
  public Uni<Ref> getOne() {
    return baseline().collect().asList().map(found -> {
      final var actual = found.size();
      if(actual != 1) {
        throw new FileSystemQueryException("Expecting exactly 1 result but found: " + actual + "!", 
          JsonObject.of(
            "branchId", branchId,
            "isNameExpr", nameExpr == null ? false : true 
          )
        );
      }
      
      return found.stream().findFirst().get();
    });
  }

}
