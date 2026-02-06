package io.resys.thena.fs.spi.queries;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.branches.BranchQuery;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
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
  
  private

  @Override
  public Uni<Optional<Ref>> findOne() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Multi<Ref> findAll() {
    return db_uni.onItem().transformToMulti(db -> db.query().queryRef().findAllByFilter(null));
  }

  @Override
  public Uni<Ref> getOne() {
    // TODO Auto-generated method stub
    return null;
  }

}
