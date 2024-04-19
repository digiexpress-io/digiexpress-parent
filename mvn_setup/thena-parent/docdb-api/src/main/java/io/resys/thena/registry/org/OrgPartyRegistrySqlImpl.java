package io.resys.thena.registry.org;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgParty;
import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.entities.org.ThenaOrgObject.OrgDocSubType;
import io.resys.thena.api.registry.org.OrgPartyRegistry;
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
public class OrgPartyRegistrySqlImpl implements OrgPartyRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple findAll(Collection<String> id) {
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
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgParties())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
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
  public ThenaSqlClient.SqlTuple insertOne(OrgParty doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append(" (id, commit_id, created_commit_id, external_id, parent_id, party_name, party_description, party_sub_type) VALUES($1, $2, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getPartyName(), doc.getPartyDescription(),  doc.getPartySubType().name()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple updateOne(OrgParty doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, party_name = $2, party_description = $3, commit_id = $4, party_sub_type = $5")
        .append(" WHERE id = $6")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getPartyName(), doc.getPartyDescription(), doc.getCommitId(), doc.getPartySubType().name(), doc.getId()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgParty> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgParties())
        .append("  (id, commit_id, created_commit_id, external_id, parent_id, party_name, party_description, party_sub_type) VALUES($1, $2, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getParentId(), doc.getPartyName(), doc.getPartyDescription(), doc.getPartySubType().name()}))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(Collection<OrgParty> users) {
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
  
  @Override
  public SqlTuple findAllByRightId(String rightId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT party.* ").ln()
        .append("FROM ").append(options.getOrgRights()).append(" as rights").ln()
        
        .append(" INNER JOIN ").append(options.getOrgPartyRights()).append(" as party_rights").ln()
        .append(" ON(rights.id = party_rights.right_id) ").ln()
        
        .append(" INNER JOIN ").append(options.getOrgParties()).append(" as party").ln()
        .append(" ON(party.id = party_rights.party_id) ").ln()
        
        .append("WHERE (rights.id = $1 OR rights.external_id = $1 OR rights.right_name = $1)").ln()
        .build())
        .props(Tuple.of(rightId))
        .build();
  }
  @Override
  public SqlTuple findAllByMemberId(String memberId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT distinct party.* ").ln()
        .append("FROM ").append(options.getOrgRights()).append(" as party").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMemberships()).append(" as memberships").ln()
        .append(" ON(party.id = memberships.party_id) ").ln()

        .append(" INNER JOIN ").append(options.getOrgMembers()).append(" as member").ln()
        .append(" ON(member.id = memberships.member_id) ").ln()
        
        .append("WHERE (member.id = $1 OR member.external_id = $1 OR member.username = $1)").ln()
        .build())
        .props(Tuple.of(memberId))
        .build();
  }

  
  @Override
  public Function<Row, OrgParty> defaultMapper() {
    return OrgPartyRegistrySqlImpl::orgParty;
  }
  private static OrgParty orgParty(Row row) {
    return ImmutableOrgParty.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .parentId(row.getString("parent_id"))
        .commitId(row.getString("commit_id"))
        .createdWithCommitId("created_commit_id")
        .partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
        .partySubType(OrgDocSubType.valueOf(row.getString("party_sub_type")))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgParties()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  parent_id VARCHAR(40),").ln()
    .append("  party_name VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  party_description VARCHAR(255) NOT NULL,").ln()
    .append("  party_sub_type VARCHAR(40) NOT NULL").ln()
    .append(");").ln().ln()
    
    // parent id, references self
    .append("ALTER TABLE ").append(options.getOrgParties()).ln()
    .append("  ADD CONSTRAINT ").append(options.getOrgParties()).append("_PARENT_FK").ln()
    .append("  FOREIGN KEY (parent_id)").ln()
    .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln()

    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_NAME_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (party_name);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgParties()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgParties()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgParties()).append(" (external_id);").ln()
    
    
    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getOrgParties()).ln()
        .append(createOrgGroupFk(options.getOrgActorData())).ln()
        .append(createOrgGroupFk(options.getOrgActorStatus())).ln()
        .append(createOrgGroupFk(options.getOrgPartyRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberRights())).ln()
        .append(createOrgGroupFk(options.getOrgMemberships())).ln()
        
        .append("ALTER TABLE ").append(options.getOrgMemberRights()).ln()
        .append("  ADD CONSTRAINT ").append(options.getOrgMemberRights()).append("_PARTY_MEMBER_FK").ln()
        .append("  FOREIGN KEY (party_id, member_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMemberships()).append(" (party_id, member_id);").ln().ln()
                
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgParties()).append(";").ln()
        .build()).build();
  }
  private String createOrgGroupFk(String tableNameThatPointToCommits) {
    return new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_PARTY_FK").ln()
        .append("  FOREIGN KEY (party_id)").ln()
        .append("  REFERENCES ").append(options.getOrgParties()).append(" (id);").ln().ln()
        .build();
  }
}
