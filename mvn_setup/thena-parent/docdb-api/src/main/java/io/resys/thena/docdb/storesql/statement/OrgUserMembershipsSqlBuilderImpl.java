package io.resys.thena.docdb.storesql.statement;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMembership;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.storesql.ImmutableSql;
import io.resys.thena.docdb.storesql.ImmutableSqlTuple;
import io.resys.thena.docdb.storesql.ImmutableSqlTupleList;
import io.resys.thena.docdb.storesql.SqlBuilder.OrgUserMembershipsSqlBuilder;
import io.resys.thena.docdb.storesql.SqlBuilder.Sql;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgUserMembershipsSqlBuilderImpl implements OrgUserMembershipsSqlBuilder {
  private final DbCollections options;
  
  @Override
  public SqlTuple findAll(List<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgMemberships()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append("id = $").append(index);
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
        .append("SELECT * FROM ").append(options.getOrgMemberships())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberships()).ln()
        .append("  WHERE id = $1").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  

	@Override
	public SqlTuple findAllByGroupId(String groupId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberships()).ln()
        .append("  WHERE party_id = $1").ln() 
        .build())
        .props(Tuple.of(groupId))
        .build();
	}

	@Override
	public SqlTuple findAllByUserId(String userId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberships()).ln()
        .append("  WHERE member_id = $1").ln() 
        .build())
        .props(Tuple.of(userId))
        .build();
	}
  @Override
  public SqlTuple insertOne(OrgMembership doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberships())
        .append(" (id, commit_id, party_id, member_id) VALUES($1, $2, $3, $4)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getPartyId(), doc.getMemberId() }))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgMembership> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberships())
        .append(" (id, commit_id, party_id, member_id) VALUES($1, $2, $3, $4)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getPartyId(), doc.getMemberId() }))
            .collect(Collectors.toList()))
        .build();
  }
}
