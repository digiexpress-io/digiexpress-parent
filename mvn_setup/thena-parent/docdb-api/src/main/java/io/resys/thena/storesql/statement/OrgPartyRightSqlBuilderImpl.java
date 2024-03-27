package io.resys.thena.storesql.statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.OrgPartyRightSqlBuilder;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgPartyRightSqlBuilderImpl implements OrgPartyRightSqlBuilder {
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
        .append("SELECT * FROM ").append(options.getOrgPartyRights())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
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
	public SqlTuple findAllByGroupId(String userId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgPartyRights()).ln()
        .append("  WHERE party_id = $1").ln() 
        .build())
        .props(Tuple.of(userId))
        .build();
	}
	@Override
	public SqlTuple findAllByRoleId(String userId) {
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
  public SqlTuple insertOne(OrgPartyRight doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgPartyRights())
        .append(" (id, commit_id, party_id, right_id) VALUES($1, $2, $3, $4)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getPartyId(), doc.getRightId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgPartyRight> users) {
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
  public SqlTupleList deleteAll(Collection<OrgPartyRight> roles) {
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
}
