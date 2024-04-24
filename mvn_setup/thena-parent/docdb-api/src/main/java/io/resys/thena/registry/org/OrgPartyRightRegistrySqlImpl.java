package io.resys.thena.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgPartyRight;
import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.registry.org.OrgPartyRightRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgPartyRightRegistrySqlImpl implements OrgPartyRightRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple findAll(List<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgMemberRights()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (")
        .append("id = $").append(index)
      .append(")");
      index++;
    }
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(id))
        .build();
  }
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgPartyRights())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgPartyRights()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

	@Override
	public ThenaSqlClient.SqlTuple findAllByPartyId(String partyId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgPartyRights()).ln()
        .append("  WHERE party_id = $1").ln() 
        .build())
        .props(Tuple.of(partyId))
        .build();
	}
	@Override
	public ThenaSqlClient.SqlTuple findAllByRoleId(String userId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgPartyRights()).ln()
        .append("  WHERE right_id = $1").ln() 
        .build())
        .props(Tuple.of(userId))
        .build();
	}
  @Override
  public ThenaSqlClient.SqlTuple insertOne(OrgPartyRight doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgPartyRights())
        .append(" (id, commit_id, party_id, right_id) VALUES($1, $2, $3, $4)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getPartyId(), doc.getRightId()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgPartyRight> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgPartyRights())
        .append(" (id, commit_id, party_id, right_id) VALUES($1, $2, $3, $4)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getPartyId(), doc.getRightId()}))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgPartyRight> roles) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getOrgPartyRights())
        .append(" WHERE id = $1").ln()
        .build())
        .props(roles.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId() }))
            .collect(Collectors.toList()))
        .build();
  }
  private static OrgPartyRight orgPartyRright(Row row) {
    return ImmutableOrgPartyRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .build();
  }
  @Override
  public Function<Row, OrgPartyRight> defaultMapper() {
    return OrgPartyRightRegistrySqlImpl::orgPartyRright;
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgPartyRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  UNIQUE (right_id, party_id)").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (party_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgPartyRights()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgPartyRights()).append(" (right_id);").ln()    

    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    
    return ImmutableSql.builder().value(new SqlStatement()
        
        .append("DROP TABLE IF EXISTS ").append(options.getPrefix()).append("org_actor_data;").ln() // TODO:: can be removed
        .append("DROP TABLE IF EXISTS ").append(options.getPrefix()).append("org_actor_status;").ln() // TODO:: can be removed
        
        .append("DROP TABLE ").append(options.getOrgPartyRights()).append(";").ln()
        .build()).build();
  }

}
