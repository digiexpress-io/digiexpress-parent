package io.resys.thena.storesql.statement;

import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.api.models.ThenaOrgObject.OrgCommit;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableSql;
import io.resys.thena.storesql.ImmutableSqlTuple;
import io.resys.thena.storesql.ImmutableSqlTupleList;
import io.resys.thena.storesql.SqlBuilder.OrgCommitSqlBuilder;
import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitSqlBuilderImpl implements OrgCommitSqlBuilder {
  private final DbCollections options;
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgMembers())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommits()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgCommit doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getParentId(), doc.getCreatedAt(), doc.getLog(), doc.getAuthor(), doc.getMessage() }))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgCommit> docs) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommits())
        .append(" (commit_id, parent_id, created_at, commit_log, commit_author, commit_message) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(docs.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getParentId(), doc.getCreatedAt(), doc.getLog(), doc.getAuthor(), doc.getMessage() }))
            .collect(Collectors.toList()))
        .build();
  }
}
