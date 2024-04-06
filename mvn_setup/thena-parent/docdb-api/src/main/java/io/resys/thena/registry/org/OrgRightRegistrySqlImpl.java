package io.resys.thena.registry.org;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgRight;
import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.api.registry.org.OrgRightRegistry;
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
public class OrgRightRegistrySqlImpl implements OrgRightRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple findAll(Collection<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgRights()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (")
        .append("id = $").append(index)
        .append(" OR external_id = $").append(index)
        .append(" OR right_name = $").append(index)
      .append(")");
      index++;
    }
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(new ArrayList<>(id)))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple findAllByPartyId(String partyId) {
    final var sql = new SqlStatement()
      .append("SELECT rights.* ").ln()
      .append("  FROM ").append(options.getOrgPartyRights()).append(" as party ").ln()
      .append("  LEFT JOIN ").append(options.getOrgRights()).append(" as rights ").ln()
      .append("  ON (rights.id = party.right_id)")
      .append("  WHERE party.party_id = $1").ln();
    
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.of(partyId))
        .build();
  }
  
  
  
  @Override
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgRights())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgRights()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR right_name = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(OrgRight doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgRights())
        .append(" (id, commit_id, external_id, right_name, right_description) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getRightName(), doc.getRightDescription(), doc.getRightSubType().name() }))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple updateOne(OrgRight doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgRights())
        .append(" SET external_id = $1, right_name = $2, right_description = $3, commit_id = $4, right_sub_type = $5")
        .append(" WHERE id = $6")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getRightName(), doc.getRightDescription(), doc.getCommitId(), doc.getId(), doc.getRightSubType().name()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgRight> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgRights())
        .append(" (id, commit_id, external_id, right_name, right_description, right_sub_type) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getRightName(), doc.getRightDescription(), doc.getRightSubType().name() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(Collection<OrgRight> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgRights())
        .append(" SET external_id = $1, right_name = $2, right_description = $3, commit_id = $4, right_sub_type = $5")
        .append(" WHERE id = $6")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getExternalId(), doc.getRightName(), doc.getRightDescription(), doc.getCommitId(), doc.getRightSubType().name(), doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }
  
  private static OrgRight orgRight(Row row) {
    return ImmutableOrgRight.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .rightSubType(OrgDocSubType.valueOf(row.getString("right_sub_type")))
        .commitId(row.getString("commit_id"))
        .build();
  }
  @Override
  public Function<Row, OrgRight> defaultMapper() {
    return OrgRightRegistrySqlImpl::orgRight;
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  right_sub_type VARCHAR(40) NOT NULL,").ln()
    .append("  right_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  right_description VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (right_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgRights()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgRights()).append(" (external_id);").ln()
    

    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append(createOrgRoleFk(options.getOrgActorData())).ln()
        .append(createOrgRoleFk(options.getOrgActorStatus())).ln()
        
        .append(createOrgRoleFk(options.getOrgMemberRights())).ln()
        .append(createOrgRoleFk(options.getOrgPartyRights())).ln()
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgRights()).append(";").ln()
        .build()).build();
  }
  private String createOrgRoleFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_RIGHT_FK").ln()
        .append("  FOREIGN KEY (right_id)").ln()
        .append("  REFERENCES ").append(options.getOrgRights()).append(" (id);").ln().ln()
        .build();
  }

}
