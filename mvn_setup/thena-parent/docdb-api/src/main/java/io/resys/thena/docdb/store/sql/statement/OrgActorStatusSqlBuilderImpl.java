package io.resys.thena.docdb.store.sql.statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgActorStatus;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.store.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.store.sql.SqlBuilder.OrgActorStatusSqlBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgActorStatusSqlBuilderImpl implements OrgActorStatusSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(List<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgActorStatus()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (").append("id = $").append(index).append(")");
      index++;
    }
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(id))
        .build();
  }
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgActorStatus())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorStatus()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgActorStatus doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgActorStatus())
        .append(" (id, commit_id, user_id, right_id, party_id, actor_status) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getUserId(), doc.getRoleId(), doc.getGroupId(), doc.getValue().name() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgActorStatus doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgActorStatus())
        .append(" SET actor_status = $1, commit_id = $2 ")
        .append(" WHERE id = $3")
        .build())
        .props(Tuple.from(new Object[]{doc.getValue().name(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgActorStatus> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgActorStatus())
        .append(" (id, commit_id, user_id, right_id, party_id, actor_status) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getUserId(), doc.getRoleId(), doc.getGroupId(), doc.getValue().name() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgActorStatus> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgActorStatus())
        .append(" SET actor_status = $1, commit_id = $2 ")
        .append(" WHERE id = $3")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getValue().name(), doc.getCommitId(), doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }
}
