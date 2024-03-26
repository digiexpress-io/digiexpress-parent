package io.resys.thena.docdb.storesql.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storesql.ImmutableSql;
import io.resys.thena.docdb.storesql.ImmutableSqlTuple;
import io.resys.thena.docdb.storesql.ImmutableSqlTupleList;
import io.resys.thena.docdb.storesql.SqlBuilder.OrgMemberSqlBuilder;
import io.resys.thena.docdb.storesql.SqlBuilder.Sql;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgMemberSqlBuilderImpl implements OrgMemberSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(Collection<String> id) {
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
        .append("  FROM ").append(options.getOrgMembers()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR username = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgMember doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMembers())
        .append(" (id, commit_id, external_id, username, email) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgMember doc) {
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
  public SqlTupleList insertAll(Collection<OrgMember> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMembers())
        .append(" (id, commit_id, external_id, username, email) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgMember> users) {
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
	public SqlTuple findAllUserPartiesAndRightsByMemberId(String userId) {
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
  public SqlTuple findAllRightsByMemberId(String userId) {
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
  public SqlTuple getStatusByUserId(String userId) {
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
}
