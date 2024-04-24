package io.resys.thena.registry.doc;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.entities.doc.DocCommitTree.DocCommitTreeOperation;
import io.resys.thena.api.entities.doc.ImmutableDocCommitTree;
import io.resys.thena.api.registry.doc.DocCommitTreeRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
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
        .append("SELECT doc_log.* ").ln()
        .append("  FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT doc_log.* ").ln()
        .append("  FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  WHERE doc_log.id = $1").ln()
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByDocIdsAndBranch(Collection<String> id, String branchId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT doc_log.* ").ln()
        .append("  FROM ").append(options.getDocLog()).append(" AS doc_log").ln()

        .append(" INNER JOIN ").append(options.getDoc()).append(" as docs").ln()
        .append(" ON(docs.id = doc_log.doc_id)")
        
        .append(" LEFT JOIN ").append(options.getDocBranch()).append(" as branches").ln()
        .append(" ON(branches.branch_id = doc_log.branch_id OR doc_log.branch_id IS NULL)")

        .append(" WHERE ").ln() 
        .append(" (docs.id = ANY($1) or docs.external_id = ANY($1)) ").ln()
        .append(" AND ").ln()
        .append(" (branch.id IS NULL OR branch.branch_name = $2 OR branch.id = $2)").ln()
        
        .build())
        .props(Tuple.of(id, branchId))
        .build();
  }
  @Override
  public SqlTuple findAllByCommitIds(List<String> commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT doc_log.* ").ln()
        .append("  FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  WHERE doc_log.commit_id = ANY($1)").ln()
        .build())
        .props(Tuple.of(commitId))
        .build();
  }

  
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<DocCommitTree> logs) {
    return ImmutableSqlTupleList.builder()
    .value(new SqlStatement()
    .append("INSERT INTO ").append(options.getDocLog())
    .append(" (id, commit_id, doc_id, branch_id, operation_type, body_after, body_before, body_patch)").ln()
    .append(" VALUES($1, $2, $3, $4, $5, $6, $7, $8)").ln()
    .build())
    .props(logs.stream()
        .map(doc -> Tuple.from(Arrays.asList(
            doc.getId(), 
            doc.getCommitId(), 
            doc.getDocId(),
            doc.getBranchId().orElse(null), 
            doc.getOperationType().name(),
            doc.getBodyAfter(), 
            doc.getBodyBefore(), 
            doc.getBodyPatch()
        )))
        .collect(Collectors.toList()))
    .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getDocLog()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    
    .append("  doc_id VARCHAR(40) NOT NULL,").ln()
    .append("  branch_id VARCHAR(40),").ln()
    .append("  operation_type VARCHAR(100) NOT NULL,").ln()
    
    .append("  body_after jsonb,").ln()
    .append("  body_before jsonb,").ln()
    .append("  body_patch jsonb").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getDocLog()).append("_DOC_INDEX")
    .append(" ON ").append(options.getDocLog()).append(" (doc_id);").ln()
    
    .append("CREATE INDEX ").append(options.getDocLog()).append("_BRANCH_INDEX")
    .append(" ON ").append(options.getDocLog()).append(" (branch_id);").ln()
    
    .append("CREATE INDEX ").append(options.getDocLog()).append("_COMMIT_INDEX")
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
  public Function<Row, DocCommitTree> defaultMapper() {
    return row -> ImmutableDocCommitTree.builder()
      .id(row.getString("id"))
      .commitId(row.getString("commit_id"))
      .docId(row.getString("doc_id"))
      .branchId(Optional.ofNullable(row.getString("branch_id")))
      .operationType(DocCommitTreeOperation.valueOf(row.getString("operation_type")))
      .bodyAfter(row.getJsonObject("body_after"))
      .bodyBefore(row.getJsonObject("body_before"))
      .bodyPatch(row.getJsonObject("body_patch"))
      .build();
  }  
  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getDocLog()).append(";").ln()
        .build()).build();
  }
}
