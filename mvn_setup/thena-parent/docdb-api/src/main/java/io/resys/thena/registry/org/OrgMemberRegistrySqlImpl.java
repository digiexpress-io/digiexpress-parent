package io.resys.thena.registry.org;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgRightFlattened;
import io.resys.thena.api.entities.org.OrgActorStatusType;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgRightFlattened;
import io.resys.thena.api.registry.org.OrgMemberRegistry;
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
public class OrgMemberRegistrySqlImpl implements OrgMemberRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple findAll(Collection<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgMembers()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (")
        .append("id = $").append(index)
        .append(" OR external_id = $").append(index)
        .append(" OR username = $").append(index)
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
        .append("SELECT * FROM ").append(options.getOrgMembers())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMembers()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR username = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMember> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMembers())
        .append(" (id, commit_id, created_commit_id, external_id, username, email, member_status, member_data_extension)").ln()
        .append(" VALUES($1, $2, $2, $3, $4, $5, $6, $7)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getId(), 
                doc.getCommitId(), 
                doc.getExternalId(), 
                doc.getUserName(), 
                doc.getEmail(), 
                doc.getStatus(), 
                doc.getDataExtension() 
             }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(Collection<OrgMember> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, username = $2, email = $3, commit_id = $4, member_status = $5, member_data_extension = $6 ")
        .append(" WHERE id = $7")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ 
                doc.getExternalId(), 
                doc.getUserName(), 
                doc.getEmail(), 
                doc.getCommitId(),
                doc.getStatus(), 
                doc.getDataExtension(), 
                doc.getId() 
             }))
            .collect(Collectors.toList()))
        .build();
  }

/*
-- basic recursive query from child to parent
SELECT * FROM org_group;

WITH RECURSIVE child AS (
  SELECT 
    id, 
    party_name, 
    parent_id
  FROM org_group
  WHERE id = 9 

  UNION 

  SELECT 
    parent.id, 
    parent.party_name, 
    parent.parent_id 
  FROM org_group as parent 
  INNER JOIN child on (parent.id = child.parent_id)
) 
SELECT * FROM child;
*/
  
	@Override
	public ThenaSqlClient.SqlTuple findAllUserPartiesAndRightsByMemberId(String userId) {
    final var sql = new SqlStatement()
        .append("WITH RECURSIVE child AS (").ln()
        
        // starting point
        .append("  SELECT id, parent_id, party_name, party_description, party_status, party_data_extension").ln()
        .append("  FROM ").append(options.getOrgParties()).ln()
        .append("  WHERE id in( ").ln()
        .append("    SELECT DISTINCT party_id ")
        .append("    FROM ").append(options.getOrgMemberships()).ln()
        .append("    WHERE member_id = $1")
        .append("  )")
        
        .append("  UNION ALL ").ln()
        
        // recursion from bottom to up, join parent to each child until the tip
        .append("  SELECT parent.id, parent.parent_id, parent.party_name, parent.party_description, parent.party_status, parent.party_data_extension").ln()
        .append("  FROM ").append(options.getOrgParties()).append(" as parent").ln()
        .append("  INNER JOIN child on (parent.id = child.parent_id) ").ln()
        
    		.append(")").ln()
    		
        .append("SELECT ").ln()
        .append("  parties.id                as id, ").ln()
        .append("  parties.parent_id         as parent_id, ").ln()
        .append("  parties.party_name        as party_name, ").ln()
        .append("  parties.party_description as party_description, ").ln()
        .append("  parties.party_status      as party_status, ").ln()

        .append("  direct_memberships.id        as membership_id, ").ln()
        .append("  direct_memberships.member_id as member_id, ").ln()
        
        .append("  rights.id                as right_id, ").ln()
        .append("  rights.right_name        as right_name, ").ln()
        .append("  rights.right_description as right_description, ").ln()
        .append("  rights.right_status      as right_status ").ln()
        
        .append("FROM ").ln()
        .append("  (SELECT DISTINCT id, parent_id, party_name, party_description, party_status, party_data_extension from child) as parties").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgMemberships()).append(" as direct_memberships").ln()        
        .append("  ON(direct_memberships.party_id = parties.id and direct_memberships.member_id = $1) ").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgPartyRights()).append(" as party_rights").ln()
        .append("  ON(party_rights.party_id = parties.id) ").ln()
    
        .append("  LEFT JOIN ").append(options.getOrgRights()).append(" as rights").ln()
        .append("  ON(rights.id = party_rights.right_id)").ln()
        ;
    
        
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.of(userId))
        .build();
	}

  @Override
  public ThenaSqlClient.SqlTuple findAllRightsByMemberId(String userId) {
    final var sql = new SqlStatement()
        
        .append("SELECT ").ln()
        .append("  rights.id                   as right_id, ").ln()
        .append("  rights.right_name           as right_name, ").ln()
        .append("  rights.right_description    as right_description, ").ln()
        .append("  rights.right_status         as right_status, ").ln()
        .append("  member_rights.party_id      as party_id ").ln()

        .append("FROM ").ln()
        .append("  ").append(options.getOrgRights()).append(" as rights").ln()
        .append("INNER JOIN ").append(options.getOrgMemberRights()).append(" as member_rights").ln()
        .append("  ON(").ln()
        .append("    member_rights.right_id = rights.id").ln()
        .append("    and member_rights.member_id = $1").ln()
        .append("    and member_rights.party_id is null").ln()
        .append("  )").ln();

    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.of(userId))
        .build();
  }


  @Override
  public SqlTuple findAllByPartyId(String partyId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT users.* ").ln()
        .append("FROM ").append(options.getOrgParties()).append(" as party").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMemberships()).append(" as memberships").ln()
        .append(" ON(party.id = memberships.party_id) ").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMembers()).append(" as users").ln()
        .append(" ON(users.id = memberships.member_id) ").ln()
        
        .append("WHERE (party.id = $1 OR party.external_id = $1 OR party.party_name = $1)").ln()
        .build())
        .props(Tuple.of(partyId))
        .build();
  }
  
  @Override
  public SqlTuple findAllByRightId(String rightId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT distinct members.* ").ln()
        .append("FROM ").append(options.getOrgRights()).append(" as rights").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMemberRights()).append(" as member_rights").ln()
        .append(" ON(rights.id = member_rights.right_id and member_rights.party_id is null) ").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMembers()).append(" as members").ln()
        .append(" ON(members.id = member_rights.member_id) ").ln()
        
        .append("WHERE (rights.id = $1 OR rights.external_id = $1 OR rights.right_name = $1)").ln()
        .build())
        .props(Tuple.of(rightId))
        .build();
  }
  @Override
  public Function<Row, OrgRightFlattened> rightFlattenedMapper() {
    return OrgMemberRegistrySqlImpl::orgRightFlattened;
  }
  @Override
  public Function<Row, OrgMember> defaultMapper() {
    return OrgMemberRegistrySqlImpl::orgMember;
  }
  private static OrgMember orgMember(Row row) {
    return ImmutableOrgMember.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .createdWithCommitId("created_commit_id")
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .dataExtension(row.getJsonObject("member_data_extension"))
        .status(OrgActorStatusType.valueOf(row.getString("member_status")))
        .build();
  }
  
  private static OrgRightFlattened orgRightFlattened(Row row) {
    return ImmutableOrgRightFlattened.builder()
        .rightId(row.getString("right_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .rightStatus(OrgActorStatusType.valueOf(row.getString("right_status")))
        .build();
  }
  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMembers()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  created_commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  external_id VARCHAR(40) UNIQUE,").ln()
    .append("  member_status VARCHAR(40) NOT NULL,").ln()
    .append("  member_data_extension JSONB,").ln()    
    .append("  username VARCHAR(255) UNIQUE NOT NULL,").ln()
    .append("  email VARCHAR(255) NOT NULL").ln()
    .append(");").ln()
    
    
    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_EXTERNAL_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (external_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMembers()).append("_MEMBER_NAME_INDEX")
    .append(" ON ").append(options.getOrgMembers()).append(" (username);").ln()

    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value(new SqlStatement()
        .ln().append("--- constraints for").append(options.getOrgMembers()).ln()
        .append(createOrgUserFk(options.getOrgMemberships())).ln()
        .append(createOrgUserFk(options.getOrgMemberRights())).ln()
        
        .build())
        .build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgMembers()).append(";").ln()
        .build()).build();
  }


  private String createOrgUserFk(String tableNameThatPointToCommits) {
    return  new SqlStatement().ln()
        .append("ALTER TABLE ").append(tableNameThatPointToCommits).ln()
        .append("  ADD CONSTRAINT ").append(tableNameThatPointToCommits).append("_MEMBER_FK").ln()
        .append("  FOREIGN KEY (member_id)").ln()
        .append("  REFERENCES ").append(options.getOrgMembers()).append(" (id);").ln().ln()
        .build();
  }
}
