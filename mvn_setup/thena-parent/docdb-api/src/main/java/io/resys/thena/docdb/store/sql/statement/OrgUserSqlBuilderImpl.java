package io.resys.thena.docdb.store.sql.statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUser;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.store.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.store.sql.SqlBuilder.OrgUserSqlBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgUserSqlBuilderImpl implements OrgUserSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(List<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgUsers()).ln()
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
        .props(Tuple.from(id))
        .build();
  }
  
  @Override
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgUsers())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgUsers()).ln()
        .append("  WHERE (id = $1 OR external_id = $1 OR username = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgUser doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgUsers())
        .append(" (id, commit_id, external_id, username, email) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgUser doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgUsers())
        .append(" SET external_id = $1, username = $2, email = $3, commit_id = $4")
        .append(" WHERE id = $5")
        .build())
        .props(Tuple.from(new Object[]{doc.getExternalId(), doc.getUserName(), doc.getEmail(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgUser> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgUsers())
        .append(" (id, commit_id, external_id, username, email) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getExternalId(), doc.getUserName(), doc.getEmail() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgUser> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgUsers())
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
    group_name, 
    parent_id
  FROM org_group
  WHERE id = 9 

  UNION 

  SELECT 
    parent.id, 
    parent.group_name, 
    parent.parent_id 
  FROM org_group as parent 
  INNER JOIN child on (parent.id = child.parent_id)
) 
SELECT * FROM child;
*/
  
	@Override
	public SqlTuple findAllUserGroupsAndRolesByUserId(String userId) {
    final var sql = new SqlStatement()
        .append("WITH RECURSIVE child AS (").ln()
        
        // starting point
        .append("  SELECT id, parent_id").ln()
        .append("  FROM ").append(options.getOrgGroups()).ln()
        .append("  WHERE id in( ").ln()
        .append("    SELECT group_id ")
        .append("    FROM ").append(options.getOrgUserMemberships()).ln()
        .append("    WHERE user_id = $1")
        .append("  )")
        
        .append("  UNION ALL ").ln()
        
        // recursion from bottom to up, join parent to each child until the tip
        .append("  SELECT parent.id, parent.parent_id").ln()
        .append("  FROM ").append(options.getOrgGroups()).append(" as parent").ln()
        .append("  INNER JOIN child on (parent.id = child.parent_id) ").ln()
        
    		.append(")").ln()
    		
        .append("SELECT ").ln()
        .append("  groups.id 				as group_id, ").ln()
        .append("  groups.parent_id as group_parent_id, ").ln()
        
        .append("  users.id as membership_id, ").ln()
        
        .append("  group_status.id           as group_status_id, ").ln()
        .append("  group_status.actor_status as group_status, ").ln()
        .append("  group_status.user_id      as group_status_user_id, ").ln()
        .append("  group_status.group_id     as group_status_group_id, ").ln()
        .append("  group_status.role_id      as group_status_role_id, ").ln()

        .append("  user_status.id           as user_status_id, ").ln()
        .append("  user_status.actor_status as user_status, ").ln()
        //.append("  user_status.user_id      as user_status_user_id, ").ln()
        .append("  user_status.group_id     as user_status_group_id, ").ln()
        .append("  user_status.role_id      as user_status_role_id ").ln()
        
        .append("FROM ").ln()
        .append("  (SELECT DISTINCT id, parent_id from child) as groups").ln()
        
        .append("  INNER JOIN ").append(options.getOrgUserMemberships()).append(" as users").ln()        
        .append("  ON(users.group_id = groups.id) ").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgActorStatus()).append(" as group_status").ln()
        .append("  ON(group_status.group_id = groups.id) ").ln()
        
        .append("  LEFT JOIN ").append(options.getOrgActorStatus()).append(" as user_status").ln()
        .append("  ON(user_status.user_id = users.user_id) ").ln()
        
        .append("WHERE ").ln()
        .append("  users.user_id = $1").ln()
        .append("  AND user_status.user_id = $1").ln()
        .append("  AND user_status.group_id IS NULL").ln()
    		;
    
    
    
    return ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.of(userId))
        .build();
	}
}
