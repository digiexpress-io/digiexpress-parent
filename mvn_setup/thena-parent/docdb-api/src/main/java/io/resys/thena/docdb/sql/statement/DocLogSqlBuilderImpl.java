package io.resys.thena.docdb.sql.statement;

import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.DocLogSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocLogSqlBuilderImpl implements DocLogSqlBuilder {
  private final DbCollections options;
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getDocLog())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getDocLog()).ln()
        .append("  WHERE id = $1").ln()
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
        .append("SELECT doc_log.* ").ln()
        .append("  FROM ").append(options.getDocLog()).append(" AS doc_log").ln()
        .append("  LEFT JOIN ").append(options.getDocCommits()).append(" AS doc_commit").ln()
        .append("  ON(doc_log.commit_id = doc_commit.id)").ln()
        .append("  WHERE doc_commit.branch_id = $1").ln()
        .build())
        .props(Tuple.of(branchId))
        .build();
  }

}
