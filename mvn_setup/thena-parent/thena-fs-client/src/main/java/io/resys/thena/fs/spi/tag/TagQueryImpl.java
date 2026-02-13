package io.resys.thena.fs.spi.tag;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.tags.TagQuery;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.fs.tables.filters.ImmutableTagTableFilter;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagQueryImpl implements TagQuery {
  private final Uni<FsDb> db_uni;
  private final String tenantId;
  
  private String tagId;
  private boolean excludeBlobs;
  private boolean excludeNodes;
  private Consumer<NameExpressionBuilder> nameExpr;

  @Override
  public TagQuery tagId(String tagId) {
    this.tagId = RepoAssert.notEmpty(tagId, () -> "tagId cannot be empty!");
    return this;
  }
  @Override
  public TagQuery excludeBlobs(boolean excludeBlobs) {
    this.excludeBlobs = excludeBlobs;
    return this;
  }
  @Override
  public TagQuery excludeNodes(boolean excludeNodes) {
    this.excludeNodes = excludeNodes;
    return this;
  }

  @Override
  public TagQuery tagName(Consumer<NameExpressionBuilder> nameExpr) {
    this.nameExpr = RepoAssert.notNull(nameExpr, () -> "nameExpr cannot be null!");
    return this;
  }
  @Override
  public Uni<Optional<Tag>> findOne() {
    return baseline().collect().asList().onItem().transformToUni(found -> {
      if(found.size() == 1) {
        return Uni.createFrom().item(found.getFirst())
            .onItem().transformToUni(this::join).map(Optional::of);
      }
      if(found.size() == 0) {
        return Uni.createFrom().item(Optional.empty());
      }
      throw new TagQueryException(
          "Expecting 0..1 tag but found " + found.size() + "" + "!", 
          JsonObject.of(
              "tenantId", tenantId,
              "tagId", tagId, 
              "isNameExpr", nameExpr != null
          ));
    });
  }
  @Override
  public Uni<Tag> getOne() {    
    return baseline().collect().asList().map(found -> {
      if(found.size() == 1) {
        return found.getFirst();
      }
      throw new TagQueryException(
          "Expecting 1 tag but found " + found.size() + "" + "!", 
          JsonObject.of(
              "tenantId", tenantId,
              "tagId", tagId, 
              "isNameExpr", nameExpr != null
          ));
    })
    .onItem().transformToUni(this::join);
  }
  @Override
  public Multi<Tag> findAll() {
    return baseline().onItem().transformToUni(this::join).concatenate();
  }
  
  private Uni<Tag> join(Tag tag) {
    final var isBlobs = !excludeBlobs && !excludeNodes;
    
    // load all 
    if(isBlobs) {
      return db_uni
        .onItem().transformToUni(tx -> tx.query().queryCommit().getByIdWithNodesAndBlobs(tag.getCommitId()))
        .map(tuple -> tag.withTransitives(tuple.getItem1(), tuple.getItem2())); 
    }
    
    // load nodes
    final var isNodes = !excludeNodes;
    if(isNodes) {
      return db_uni
        .onItem().transformToUni(tx -> tx.query().queryCommit().getByIdWithNodes(tag.getCommitId()))
        .map(tuple -> tag.withTransitives(tuple.getItem1(), tuple.getItem2()));
    }    
    return Uni.createFrom().item(tag);
  }
  
  public Multi<Tag> baseline() {
    final var filter = ImmutableTagTableFilter.builder().nameExpr(nameExpr).tagId(tagId).build();
    return db_uni.onItem().transformToMulti(tx -> tx.query().queryTag().findAllByFilter(filter));
  }
}
