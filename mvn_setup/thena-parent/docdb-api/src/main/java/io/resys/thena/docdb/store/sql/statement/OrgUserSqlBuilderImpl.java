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
WITH RECURSIVE generation AS (
    SELECT id, parent, 0 AS order_no
    FROM nested_10_commits
    WHERE parent IS NULL
UNION ALL
    SELECT child.id, child.parent, order_no+1 AS order_no
    FROM nested_10_commits as child
    JOIN generation g ON g.id = child.parent
)
   */
  
	@Override
	public SqlTuple findAllUserGroupsAndRolesByUserId(String userId) {
    final var sql = new SqlStatement()
        .append("WITH RECURSIVE generation AS (").ln()
        
        // select root object
        .append("  SELECT id, parent_id, 0 AS order_no").ln()
        .append("  FROM ").append(options.getOrgGroups()).ln()
        .append("  WHERE parent_id IS NULL ").ln()
        
        .append("  UNION ALL ").ln()
        
        // select children
        .append("  SELECT child.id, child.parent_id, order_no+1 AS order_no").ln()
        .append("  FROM ").append(options.getOrgGroups()).append(" as child").ln()
        .append("  JOIN generation g ON g.id = child.parent_id ").ln()
        
    		.append(")").ln()
    		
        .append("SELECT group_name, role_name, actor_status").ln()    		
    		
    		;
    
    
    
    return null;
    		/*
    		ImmutableSqlTuple.builder()
        .value(sql.build())
        .props(Tuple.from(userId))
        .build();
        */
	}
}
