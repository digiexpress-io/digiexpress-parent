package io.resys.thena.docdb.store.sql.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgGroup;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.store.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.store.sql.SqlBuilder.OrgGroupSqlBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgGroupSqlBuilderImpl implements OrgGroupSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(Collection<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgParties()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (")
        .append("id = $").append(index)
        .append(" OR external_id = $").append(index)
        .append(" OR party_name = $").append(index)
      .append(")");
      index++;
    }
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(new ArrayList<>(id)))
        .build();
  }
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgParties())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgParties()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR party_name = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgGroup doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append(" (id, commit_id, external_id, parent_id, party_name, party_description) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getGroupName(), doc.getGroupDescription() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgGroup doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, party_name = $2, party_description = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getGroupName(), doc.getGroupDescription(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgGroup> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append("  (id, commit_id, external_id, parent_id, party_name, party_description) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getGroupName(), doc.getGroupDescription() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgGroup> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, party_name = $2, party_description = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getExternalId(), doc.getGroupName(), doc.getGroupDescription(), doc.getCommitId(), doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }
}
