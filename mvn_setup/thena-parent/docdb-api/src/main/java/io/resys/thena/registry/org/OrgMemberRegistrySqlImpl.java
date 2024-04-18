package io.resys.thena.registry.org;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgMember;
import io.resys.thena.api.entities.org.ImmutableOrgMemberFlattened;
import io.resys.thena.api.entities.org.ImmutableOrgMemberHierarchyEntry;
import io.resys.thena.api.entities.org.ImmutableOrgRightFlattened;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.entities.org.OrgMember;
import io.resys.thena.api.entities.org.OrgMemberFlattened;
import io.resys.thena.api.entities.org.OrgMemberHierarchyEntry;
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
  public ThenaSqlClient.SqlTuple insertOne(OrgMember doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMembers())
        .append(" (id, commit_id, created_commit_id, external_id, username, email) VALUES($1, $2, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple updateOne(OrgMember doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, username = $2, email = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getUserName(), doc.getEmail(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMember> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMembers())
        .append(" (id, commit_id, created_commit_id, external_id, username, email) VALUES($1, $2, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList updateMany(Collection<OrgMember> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgMembers())
        .append(" SET external_id = $1, username = $2, email = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getExternalId(), doc.getUserName(), doc.getEmail(), doc.getCommitId(), doc.getId() }))
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
        .append("  SELECT id, parent_id, party_name, party_description").ln()
        .append("  FROM ").append(options.getOrgParties()).ln()
        .append("  WHERE id in( ").ln()
        .append("    SELECT DISTINCT party_id ")
        .append("    FROM ").append(options.getOrgMemberships()).ln()
        .append("    WHERE member_id = $1")
        .append("  )")
        
        .append("  UNION ALL ").ln()
        
        // recursion from bottom to up, join parent to each child until the tip
        .append("  SELECT parent.id, parent.parent_id, parent.party_name, parent.party_description").ln()
        .append("  FROM ").append(options.getOrgParties()).append(" as parent").ln()
        .append("  INNER JOIN child on (parent.id = child.parent_id) ").ln()
        
    		.append(")").ln()
    		
        .append("SELECT ").ln()
        .append("  groups.id                as id, ").ln()
        .append("  groups.parent_id         as parent_id, ").ln()
        .append("  groups.party_name        as party_name, ").ln()
        .append("  groups.party_description as party_description, ").ln()
        .append("  direct_memberships.id    as membership_id, ").ln()

        .append("  group_status.id              as status_id, ").ln()
        .append("  group_status.actor_status    as status, ").ln()
        .append("  group_status.member_id         as status_member_id, ").ln()

        .append("  group_roles.right_id          as right_id, ").ln()
        .append("  role.right_name               as right_name, ").ln()
        .append("  role.right_description        as right_description, ").ln()
        .append("  right_status.actor_status     as right_status, ").ln()
        .append("  right_status.id               as right_status_id ").ln()
        
        .append("FROM ").ln()
        .append("  (SELECT DISTINCT id, parent_id, party_name, party_description from child) as groups").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgMemberships()).append(" as direct_memberships").ln()        
        .append("  ON(direct_memberships.party_id = groups.id and direct_memberships.member_id = $1) ").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgActorStatus()).append(" as group_status").ln()
        .append("  ON(group_status.party_id = groups.id and (group_status.member_id is null or group_status.member_id = $1) and group_status.right_id is null) ").ln()
    
        .append("  LEFT JOIN ").append(options.getOrgPartyRights()).append(" as group_roles").ln()
        .append("  ON(group_roles.party_id = groups.id) ").ln()
    
        .append("  LEFT JOIN ").append(options.getOrgActorStatus()).append(" as right_status").ln()
        .append("  ON(right_status.party_id = groups.id ").ln()
        .append("    and right_status.right_id = group_roles.right_id ").ln()
        .append("    and (right_status.member_id is null or right_status.member_id = $1)").ln()
        .append("  ) ").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgRights()).append(" as role").ln()
        .append("  ON(role.id = group_roles.right_id)").ln()
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
        .append("  member_rights.party_id      as party_id, ").ln()
        .append("  right_status.actor_status   as right_status, ").ln()
        .append("  right_status.id             as right_status_id ").ln()
        
        .append("FROM ").ln()
        .append("  ").append(options.getOrgRights()).append(" as rights").ln()
        .append("INNER JOIN ").append(options.getOrgMemberRights()).append(" as member_rights").ln()
        .append("  ON(").ln()
        .append("    member_rights.right_id = rights.id").ln()
        .append("    and member_rights.member_id = $1").ln()
        .append("    and member_rights.party_id is null").ln()
         .append("  ) ").ln()
        
        .append("LEFT JOIN ").append(options.getOrgActorStatus()).append(" as right_status").ln()
        .append("  ON(").ln()
        .append("    right_status.right_id = rights.id").ln()
        .append("    and right_status.party_id is null").ln()
        .append("    and (right_status.member_id is null or right_status.member_id = $1)").ln()
        .append("  ) ").ln();

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
        .append("SELECT users.* ").ln()
        .append("FROM ").append(options.getOrgRights()).append(" as right").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMemberRights()).append(" as memberships").ln()
        .append(" ON(right.id = memberships.right_id and memberships.party_id is null) ").ln()
        
        .append(" INNER JOIN ").append(options.getOrgMembers()).append(" as users").ln()
        .append(" ON(users.id = memberships.member_id) ").ln()
        
        .append("WHERE (right.id = $1 OR right.external_id = $1 OR right.right_name = $1)").ln()
        .build())
        .props(Tuple.of(rightId))
        .build();
  }
  
  @Override
  public ThenaSqlClient.SqlTuple getStatusByUserId(String userId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT ").ln()
        .append("  users.* ,").ln()
        .append("  user_status.actor_status as user_status,").ln()
        .append("  user_status.id as user_status_id").ln()
        .append("FROM ").append(options.getOrgMembers()).append(" as users").ln()

        .append("LEFT JOIN ").append(options.getOrgActorStatus()).append(" as user_status").ln()
        .append("ON(").ln()
        .append("  user_status.member_id = users.id").ln()
        .append("  and user_status.right_id is null").ln()
        .append("  and user_status.party_id is null").ln()
        .append(") ").ln()
        .append("WHERE (users.id = $1 OR users.external_id = $1 OR users.username = $1)").ln()
        .build())
        .props(Tuple.of(userId))
        .build();
  }
  

  @Override
  public Function<Row, OrgRightFlattened> rightFlattenedMapper() {
    return OrgMemberRegistrySqlImpl::orgRightFlattened;
  }
  @Override
  public Function<Row, OrgMemberFlattened> memberFlattenedMapper() {
    return OrgMemberRegistrySqlImpl::orgMemberFlattened;
  }
  @Override
  public Function<Row, OrgMemberHierarchyEntry> memberHierarchyEntryMapper() {
    return OrgMemberRegistrySqlImpl::orgMemberHierarchyEntry;
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
        .build();
  }
  private static OrgMemberHierarchyEntry orgMemberHierarchyEntry(Row row) {
    final var roleStatus = row.getString("right_status");
    final var groupStatus = row.getString("status");
    
    return ImmutableOrgMemberHierarchyEntry.builder()
        .partyId(row.getString("id"))
        .partyParentId(row.getString("parent_id"))
        .partyName(row.getString("party_name"))
        .partyDescription(row.getString("party_description"))
        .membershipId(row.getString("membership_id"))
        
        .partyStatusId(row.getString("status_id"))
        .partyStatus(groupStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(groupStatus) : null)
        .partyStatusMemberId(row.getString("status_member_id"))
        
        .rightId(row.getString("right_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        
        .rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
        .rightStatusId(row.getString("right_status_id"))
        .build();
  }
  
  private static OrgRightFlattened orgRightFlattened(Row row) {
    final var roleStatus = row.getString("right_status");
    return ImmutableOrgRightFlattened.builder()
        .rightId(row.getString("right_id"))
        .rightName(row.getString("right_name"))
        .rightDescription(row.getString("right_description"))
        .rightStatus(roleStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(roleStatus) : null)
        .rightStatusId(row.getString("right_status_id"))
        .build();
  }
  private static OrgMemberFlattened orgMemberFlattened(Row row) {
    final var userStatus = row.getString("user_status");
    return ImmutableOrgMemberFlattened.builder()
        .id(row.getString("id"))
        .externalId(row.getString("external_id"))
        .commitId(row.getString("commit_id"))
        .userName(row.getString("username"))
        .email(row.getString("email"))
        .status(userStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(userStatus) : null)
        .statusId(row.getString("user_status_id"))
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
        
        .append(createOrgUserFk(options.getOrgActorData())).ln()
        .append(createOrgUserFk(options.getOrgActorStatus())).ln()
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
