package io.resys.thena.docdb.file.builders;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ImmutableCommitLock;
import io.resys.thena.docdb.api.models.ThenaObject.Commit;
import io.resys.thena.docdb.api.models.ThenaObject.CommitLock;
import io.resys.thena.docdb.api.models.ThenaObject.CommitLockStatus;
import io.resys.thena.docdb.file.FileBuilder;
import io.resys.thena.docdb.file.tables.Table.FileMapper;
import io.resys.thena.docdb.file.tables.Table.FilePool;
import io.resys.thena.docdb.spi.ClientQuery.CommitQuery;
import io.resys.thena.docdb.spi.ClientQuery.LockCriteria;
import io.resys.thena.docdb.spi.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CommitQueryFilePool implements CommitQuery {
  private final FilePool client;
  private final FileMapper mapper;
  private final FileBuilder builder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Commit> getById(String commit) {
    final var sql = builder.commits().getById(commit);
    return client.preparedQuery(sql)
        .mapping(row -> mapper.commit(row))
        .execute()
        .onItem()
        .transform((Collection<Commit> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'COMMIT' by 'id': '" + commit + "'!", e));
  }
  @Override
  public Multi<Commit> findAll() {
    final var sql = builder.commits().findAll();
    return client.preparedQuery(sql)
        .mapping(row -> mapper.commit(row))
        .execute()
        .onItem()
        .transformToMulti((Collection<Commit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'COMMIT'!", e));
  }
  @Override
  public Uni<CommitLock> getLock(LockCriteria crit) {
    // final var commitId = crit.getCommitId(); 
    final var headName = crit.getHeadName();
    return new RefQueryFilePool(client, mapper, builder, errorHandler).nameOrCommit(headName).onItem().transformToUni(ref -> {
      if(ref == null) {
        return Uni.createFrom().item((CommitLock) ImmutableCommitLock.builder()
          .branch(Optional.empty())
          .commit(Optional.empty())
          .tree(Optional.empty())
          .message(Optional.empty())
          .status(CommitLockStatus.NOT_FOUND)
          .build());
      }
      
      return getById(ref.getCommit()).onItem().transformToUni(commit -> {
        final var treeUni = new TreeQueryFilePool(client, mapper, builder, errorHandler).getById(commit.getTree());
        final var blobUni = new BlobQueryFilePool(client, mapper, builder, errorHandler).findAll(commit.getTree(), Collections.emptyList()).collect().asList();        
        
        return Uni.combine().all().unis(treeUni, blobUni).asTuple().onItem().transform(tuple -> {
          return (CommitLock) ImmutableCommitLock.builder()
          .branch(ref)
          .commit(commit)
          .tree(tuple.getItem1())
          .message(Optional.empty())
          .status(CommitLockStatus.LOCK_TAKEN)
          .blobs(tuple.getItem2().stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build();
        });
      });
    });
  }
}
