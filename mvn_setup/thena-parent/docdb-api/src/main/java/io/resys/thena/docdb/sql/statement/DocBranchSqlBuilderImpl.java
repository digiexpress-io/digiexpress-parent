package io.resys.thena.docdb.sql.statement;

import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.sql.SqlBuilder.DocBranchSqlBuilder;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DocBranchSqlBuilderImpl implements DocBranchSqlBuilder {
  private final DbCollections options;

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
        .append(" (branch_id, branch_name, commit_id, doc_id, value) VALUES($1, $2, $3, $4, $5)")
        .build())
        .props(Tuple.of(ref.getId(), ref.getBranchName(), ref.getCommitId(), ref.getDocId(), ref.getValue()))
        .build();
  }

  @Override
  public SqlTuple updateOne(DocBranch ref) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getDocBranch())
        .append(" SET commit = $1, branch_name = $2, value = $3")
        .append(" WHERE branch_id = $4")
        .build())
        .props(Tuple.of(ref.getCommitId(), ref.getBranchName(), ref.getValue(), ref.getId()))
        .build();
  }
  
}
