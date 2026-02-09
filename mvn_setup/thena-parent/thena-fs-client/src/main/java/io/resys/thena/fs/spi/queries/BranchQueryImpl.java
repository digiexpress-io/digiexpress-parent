package io.resys.thena.fs.spi.queries;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
