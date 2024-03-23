package io.resys.thena.docdb.storesql.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgParty;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storesql.ImmutableSql;
import io.resys.thena.docdb.storesql.ImmutableSqlTuple;
import io.resys.thena.docdb.storesql.ImmutableSqlTupleList;
import io.resys.thena.docdb.storesql.SqlBuilder.OrgPartySqlBuilder;
import io.resys.thena.docdb.storesql.SqlBuilder.Sql;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgPartySqlBuilderImpl implements OrgPartySqlBuilder {
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
  public SqlTuple insertOne(OrgParty doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append(" (id, commit_id, external_id, parent_id, party_name, party_description) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getPartyName(), doc.getPartyDescription() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgParty doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, party_name = $2, party_description = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getPartyName(), doc.getPartyDescription(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgParty> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append("  (id, commit_id, external_id, parent_id, party_name, party_description) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getPartyName(), doc.getPartyDescription() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgParty> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgParties())
        .append(" SET external_id = $1, party_name = $2, party_description = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getExternalId(), doc.getPartyName(), doc.getPartyDescription(), doc.getCommitId(), doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }
}
