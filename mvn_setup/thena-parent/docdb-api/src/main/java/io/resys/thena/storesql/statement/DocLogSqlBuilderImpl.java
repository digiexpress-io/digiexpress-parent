package io.resys.thena.storesql.statement;

import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.DocLogSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocLogSqlBuilderImpl implements DocLogSqlBuilder {
  private final TenantTableNames options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append("  doc_log.id as id, ").ln()
        .append("  doc_log.commit_id as commit_id, ").ln()
        .append("  doc_log.value as value, ").ln()
        .append("  doc_commit.doc_id as doc_id, ").ln()
        .append("  doc_commit.branch_id as branch_id ").ln()
        
        .append("FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  LEFT JOIN ").append(options.getDocCommits()).append(" AS doc_commit").ln()
        .append("  ON(doc_log.commit_id = doc_commit.id) ").ln()
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append("  doc_log.id as id, ").ln()
        .append("  doc_log.commit_id as commit_id, ").ln()
        .append("  doc_log.value as value, ").ln()
        .append("  doc_commit.branch_id as branch_id ").ln()
        
        .append("FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  LEFT JOIN ").append(options.getDocCommits()).append(" AS doc_commit").ln()
        .append("  ON(doc_log.commit_id = doc_commit.id) ").ln()
        .append("WHERE doc_log.id = $1").ln()
        .build())
        .props(Tuple.of(id, id))
        .build();
  }
  @Override
  public SqlTuple insertOne(DocLog doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocLog())
        .append(" (id, commit_id, value) VALUES($1, $2, $3)").ln()
        .build())
        .props(Tuple.of(doc.getId(), doc.getDocCommitId(), doc.getValue()))
        .build();
  }
  @Override
  public SqlTuple findByBranchId(String branchId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT").ln()
        .append("  doc_log.id as id, ").ln()
        .append("  doc_log.commit_id as commit_id, ").ln()
        .append("  doc_log.value as value, ").ln()
        .append("  doc_commit.branch_id as branch_id ").ln()
        
        .append("FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  LEFT JOIN ").append(options.getDocCommits()).append(" AS doc_commit").ln()
        .append("  ON(doc_log.commit_id = doc_commit.id) ").ln()
        .append("WHERE doc_commit.branch_id = $1").ln()
        .build())
        .props(Tuple.of(branchId))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<DocLog> logs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocLog())
        .append(" (id, commit_id, value) VALUES($1, $2, $3)").ln()
        .build())
        .props(logs.stream()
            .map(doc -> Tuple.of(doc.getId(), doc.getDocCommitId(), doc.getValue()))
            .collect(Collectors.toList()))
        .build();
  }

}
