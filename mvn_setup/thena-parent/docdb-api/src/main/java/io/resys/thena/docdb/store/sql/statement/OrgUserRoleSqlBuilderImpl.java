package io.resys.thena.docdb.store.sql.statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgUserRole;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.store.sql.ImmutableSql;
import io.resys.thena.docdb.store.sql.ImmutableSqlTuple;
import io.resys.thena.docdb.store.sql.ImmutableSqlTupleList;
import io.resys.thena.docdb.store.sql.SqlBuilder.OrgUserRoleSqlBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.store.sql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgUserRoleSqlBuilderImpl implements OrgUserRoleSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(List<String> id) {
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
  public Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgMemberRights())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberRights()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }

	@Override
	public SqlTuple findAllByUserId(String userId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberRights()).ln()
        .append("  WHERE user_id = $1").ln() 
        .build())
        .props(Tuple.of(userId))
        .build();
	}
	@Override
	public SqlTuple findAllByRoleId(String roleId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberRights()).ln()
        .append("  WHERE right_id = $1").ln() 
        .build())
        .props(Tuple.of(roleId))
        .build();
	}
  @Override
  public SqlTuple insertOne(OrgUserRole doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberRights())
        .append(" (id, commit_id, user_id, right_id, party_id) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getUserId(), doc.getRoleId(), doc.getGroupId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgUserRole> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberRights())
        .append(" (id, commit_id, user_id, right_id, party_id) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getUserId(), doc.getRoleId(), doc.getGroupId()}))
            .collect(Collectors.toList()))
        .build();
  }
}
