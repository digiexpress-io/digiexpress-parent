package io.resys.thena.storesql.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.DocBranchSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.structures.doc.DocQueries.DocBranchLockCriteria;
import io.resys.thena.structures.doc.DocQueries.DocLockCriteria;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocBranchSqlBuilderImpl implements DocBranchSqlBuilder {
  private final TenantTableNames options;

  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocBranch())
        .build())
        .build();
  }

  @Override
  public SqlTuple getById(String branchId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocBranch())
        .append(" WHERE branch_id = $1")
        .append(" FETCH FIRST ROW ONLY")
        .build())
        .props(Tuple.of(branchId))
        .build();
  }
  @Override
  public SqlTuple insertOne(DocBranch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocBranch())
        .append(" (branch_id, branch_name, branch_status, commit_id, doc_id, value) VALUES($1, $2, $3, $4, $5, $6)")
        .build())
        .props(Tuple.of(ref.getId(), ref.getBranchName(), ref.getStatus().name(), ref.getCommitId(), ref.getDocId(), ref.getValue()))
        .build();
  }


  @Override
  public SqlTupleList insertAll(Collection<DocBranch> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocBranch())
        .append(" (branch_id, branch_name, branch_status, commit_id, doc_id, value) VALUES($1, $2, $3, $4, $5, $6)")
        .build())
        .props(docs.stream().map(ref -> {
          return Tuple.of(ref.getId(), ref.getBranchName(), ref.getStatus().name(), ref.getCommitId(), ref.getDocId(), ref.getValue());
        }) .collect(Collectors.toList()))
        .build();
  }  
  
  @Override
  public SqlTuple updateOne(DocBranch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDocBranch())
        .append(" SET commit = $1, branch_name = $2, value = $3, branch_name_deleted = $4")
        .append(" WHERE branch_id = $5")
        .build())
        .props(Tuple.of(ref.getCommitId(), ref.getBranchName(), ref.getValue(), ref.getBranchNameDeleted(), ref.getId()))
        .build();
  }
  
  @Override
  public SqlTuple getBranchLock(DocBranchLockCriteria crit) {
    final var branchName = crit.getBranchName();
    final var docId = crit.getDocId();

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE branch_name = $1 AND doc_id = $2 FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        .build())
        .props(Tuple.of(branchName, docId))
        .build();  
  }
  
  @Override
  public SqlTuple getDocLock(DocLockCriteria crit) {
    final var docId = crit.getDocId();

    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE doc_id = $1 FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id AND commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        .build())
        .props(Tuple.of(docId))
        .build();  
  }

  @Override
  public SqlTupleList updateAll(List<DocBranch> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDocBranch())
        .append(" SET commit_id = $1, branch_name = $2, value = $3, branch_name_deleted = $4")
        .append(" WHERE branch_id = $5")
        .build())        
        .props(docs.stream().map(ref -> {
          return Tuple.of(ref.getCommitId(), ref.getBranchName(), ref.getValue(), ref.getBranchNameDeleted(), ref.getId());
        }) .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTuple getBranchLocks(List<DocBranchLockCriteria> criteria) {
    final var props = new ArrayList<Object>();
    var index = 1;
    final var where = new StringBuilder();
    for(final var crit : criteria) {
      props.add(crit.getBranchName());
      props.add(crit.getDocId());
      if(index > 1) {
        where.append(" OR ");
      }
      where
        .append(" (")
        .append(" branch_name = $").append(index++)
        .append(" AND doc_id = $").append(index++)
        .append(") "); 
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
    
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE ").append(where.toString()).append(" FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        .build())
        .props(Tuple.from(props))
        .build();  
  }
  
  
  @Override
  public SqlTuple getDocLocks(List<DocLockCriteria> criteria) {
    final var props = new ArrayList<Object>();
    var index = 1;
    final var where = new StringBuilder();
    for(final var crit : criteria) {
      props.add(crit.getDocId());
      if(index > 1) {
        where.append(" OR ");
      }
      where
        .append(" (")
        .append(" doc_id = $").append(index++)
        .append(") "); 
    }
    
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ")
        .append("  doc.external_id as external_id,").ln()
        .append("  doc.external_id_deleted as external_id_deleted,").ln()
        .append("  doc.doc_type as doc_type,").ln()
        .append("  doc.doc_status as doc_status,").ln()
        .append("  doc.doc_meta as doc_meta,").ln()
        .append("  doc.doc_parent_id as doc_parent_id,").ln()
        
        .append("  branch.doc_id as doc_id,").ln()
        .append("  branch.branch_id as branch_id,").ln()
        .append("  branch.branch_name as branch_name,").ln()
        .append("  branch.branch_name_deleted as branch_name_deleted,").ln()
        .append("  branch.commit_id as branch_commit_id,").ln()
        .append("  branch.branch_status as branch_status,").ln()
        .append("  branch.value as branch_value,").ln()
        
        .append("  commits.author as author,").ln()
        .append("  commits.datetime as datetime,").ln()
        .append("  commits.message as message,").ln()
        .append("  commits.parent as commit_parent,").ln()
        .append("  commits.id as commit_id").ln()
        
        .append(" FROM (SELECT * FROM ").append(options.getDocBranch()).append(" WHERE ").append(where.toString()).append(" FOR UPDATE NOWAIT) as branch").ln()
        .append(" JOIN ").append(options.getDocCommits()).append(" as commits ON(commits.branch_id = branch.branch_id and commits.id = branch.commit_id)").ln()
        .append(" JOIN ").append(options.getDoc()).append(" as doc ON(doc.id = branch.doc_id)").ln()
        .build())
        .props(Tuple.from(props))
        .build();  
  }

}
