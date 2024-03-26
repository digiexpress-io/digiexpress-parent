package io.resys.thena.storesql.builders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.models.ImmutableDocLock;
import io.resys.thena.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.api.models.ThenaDocObject.DocBranchLock;
import io.resys.thena.api.models.ThenaDocObject.DocLock;
import io.resys.thena.api.models.ThenaGitObject.CommitLockStatus;
import io.resys.thena.models.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.models.doc.DocQueries.DocBranchQuery;
import io.resys.thena.models.doc.DocQueries.DocLockCriteria;
import io.resys.thena.storesql.SqlBuilder;
import io.resys.thena.storesql.SqlMapper;
import io.resys.thena.storesql.support.SqlClientWrapper;
import io.resys.thena.support.ErrorHandler;
import io.resys.thena.support.ErrorHandler.SqlFailed;
import io.resys.thena.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class DocBranchQuerySqlPool implements DocBranchQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<DocBranch> getById(String id) {
    final var sql = sqlBuilder.docBranches().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("DocBranch byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranch(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranch> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'DOC_BRANCH' by 'id': '" + id + "'!", sql, e)));
  }
  @Override
  public Multi<DocBranch> findAll() {
    final var sql = sqlBuilder.docBranches().findAll();
    if(log.isDebugEnabled()) {
      log.debug("DocLog findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranch(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<DocBranch> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'DOC_BRANCH'!", sql, e)));
  }
  
  @Override
  public Uni<DocBranchLock> getBranchLock(DocBranchLockCriteria crit) {
    final var sql = sqlBuilder.docBranches().getBranchLock(crit);
    if(log.isDebugEnabled()) {
      log.debug("DocBranch: {} getLock for ONE branch query, with props: {} \r\n{}",
          DocCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranchLock(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranchLock> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't lock branch: '" + crit.getDocId() + "/" + crit.getBranchName() + "'!", sql, e)));
  }
  @Override
  public Uni<DocLock> getDocLock(DocLockCriteria crit) {
    final var sql = sqlBuilder.docBranches().getDocLock(crit);
    if(log.isDebugEnabled()) {
      log.debug("DocBranch: {} getLock for ALL doc branches query, with props: {} \r\n{}",
          DocCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    if(crit.getDocId().isEmpty()) {
      return Uni.createFrom().item(ImmutableDocLock.builder().status(CommitLockStatus.NOT_FOUND).build());
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranchLock(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranchLock> rowset) -> {
          final var builder = ImmutableDocLock.builder().status(CommitLockStatus.NOT_FOUND);
          final var it = rowset.iterator();
          while(it.hasNext()) {
            final var branch = it.next();
            builder
              .status(CommitLockStatus.LOCK_TAKEN)
              .doc(branch.getDoc())
              .addBranches(branch);
          }
          
          final DocLock lock = builder.build();
          return lock;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't lock branches for doc: '" + crit.getDocId() + "'!", sql, e)));
  }
  @Override
  public Uni<List<DocBranchLock>> getBranchLocks(List<DocBranchLockCriteria> crit) {
    final var sql = sqlBuilder.docBranches().getBranchLocks(crit);
    if(log.isDebugEnabled()) {
      log.debug("DocBranch: {} getLocks for branches query, with props: {} \r\n{}",
          DocCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranchLock(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranchLock> rowset) -> {
          final var result = new ArrayList<DocBranchLock>();
          final var it = rowset.iterator();
          while(it.hasNext()) {
            result.add(it.next());
          }
          
          return Collections.unmodifiableList(result);
        })
        .onFailure().invoke(e -> {
          final var source = crit.stream().map(i -> i.getDocId() + "/" + i.getBranchName()).toList();
          errorHandler.deadEnd(new SqlTupleFailed("Can't lock branch for docs: '" + String.join(", ", source) + "'!", sql, e));
        });
  }
  
  @Override
  public Uni<List<DocLock>> getDocLocks(List<DocLockCriteria> crit) {
    final var sql = sqlBuilder.docBranches().getDocLocks(crit);
    if(log.isDebugEnabled()) {
      log.debug("Doc: {} getLocks for ALL branches query, with props: {} \r\n{}",
          DocCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.docBranchLock(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<DocBranchLock> rowset) -> {
          final var result = new HashMap<String, ImmutableDocLock.Builder>();
          final var it = rowset.iterator();
          while(it.hasNext()) {
            final var lock = it.next();
            if(lock.getDoc().isEmpty()) {
              continue;
            }
            final var docId = lock.getDoc().get().getId();
            if(!result.containsKey(docId)) {
              result.put(docId, ImmutableDocLock.builder().doc(lock.getDoc()).status(CommitLockStatus.LOCK_TAKEN));
            }
            result.get(docId).addBranches(lock);
          }
          
          return result.values().stream()
              .map(b -> (DocLock) b.build())
              .collect(Collectors.toList());
        })
        .onFailure().invoke(e -> {
          final var source = crit.stream().map(i -> i.getDocId()).toList();
          errorHandler.deadEnd(new SqlTupleFailed("Can't lock branch for docs: '" + String.join(", ", source) + "'!", sql, e));
        });
  }
}
