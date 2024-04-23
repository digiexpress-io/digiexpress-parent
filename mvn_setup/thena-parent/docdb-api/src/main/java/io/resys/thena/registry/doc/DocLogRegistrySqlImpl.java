package io.resys.thena.registry.doc;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocLog;
import io.resys.thena.api.entities.doc.ImmutableDocLog;
import io.resys.thena.api.registry.doc.DocCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocLogRegistrySqlImpl implements DocCommitTreeRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.Sql findAll() {
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
  public ThenaSqlClient.SqlTuple getById(String id) {
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
  public ThenaSqlClient.SqlTuple insertOne(DocLog doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocLog())
        .append(" (id, commit_id, value) VALUES($1, $2, $3)").ln()
        .build())
        .props(Tuple.of(doc.getId(), doc.getDocCommitId(), doc.getValue()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findByBranchId(String branchId) {
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
  public ThenaSqlClient.SqlTupleList insertAll(Collection<DocLog> logs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getDocLog())
        .append(" (id, commit_id, value) VALUES($1, $2, $3)").ln()
        .build())
        .props(logs.stream()
            .map(doc -> Tuple.diff(doc.getId(), doc.getDocCommitId(), doc.getValue()))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, DocLog> defaultMapper() {
    return DocLogRegistrySqlImpl::docLog;
  }

  private static DocLog docLog(Row row) {
    return ImmutableDocLog.builder()
        .id(row.getString("id"))
        .docId(row.getString("doc_id"))
        .branchId(row.getString("branch_id"))
        .docCommitId(row.getString("commit_id"))
        .value(jsonObject(row, "value"))
        .build();
  }
  private static JsonObject jsonObject(Row row, String columnName) {
    // string based - new JsonObject(row.getString(columnName));
    return row.getJsonObject(columnName);
  }
  
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocLog()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  value jsonb NOT NULL").ln()
    .append(");").ln()
    

    .append("CREATE INDEX ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_ID_INDEX")
    .append(" ON ").append(options.getDocLog()).append(" (commit_id);").ln()
    
    .build()).build();
  }
  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder()
        .value(new SqlStatement().ln()
        .append("ALTER TABLE ").append(options.getDocLog()).ln()
        .append("  ADD CONSTRAINT ").append(options.getDocLog()).append("_DOC_LOG_COMMIT_FK").ln()
        .append("  FOREIGN KEY (commit_id)").ln()
        .append("  REFERENCES ").append(options.getDocCommits()).append(" (id);").ln().ln()
        
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDocLog()).append(";").ln()
        .build()).build();
  }

}
