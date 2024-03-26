package io.resys.thena.storesql.statement;

import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ThenaOrgObject.OrgCommitTree;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.ImmutableSql;
import io.resys.thena.storesql.ImmutableSqlTuple;
import io.resys.thena.storesql.ImmutableSqlTupleList;
import io.resys.thena.storesql.SqlBuilder.OrgCommitTreeSqlBuilder;
import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgCommitTreeSqlBuilderImpl implements OrgCommitTreeSqlBuilder {
  private final DbCollections options;
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgCommitTrees())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgCommitTree> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgCommitTrees())
        .append(" (id, commit_id, parent_commit_id, actor_id, actor_type, value) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getParentCommitId(), doc.getActorId(), doc.getActorType(), doc.getValue() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTuple findByCommmitId(String commitId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgCommitTrees()).ln()
        .append("  WHERE commit_id = $1").ln() 
        .build())
        .props(Tuple.of(commitId))
        .build();
  }
}
