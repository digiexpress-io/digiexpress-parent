package io.resys.thena.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgActorStatus;
import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.registry.org.OrgActorStatusRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgActorStatusRegistrySqlImpl implements OrgActorStatusRegistry {
  private final TenantTableNames options;
  
  @Override
  public SqlTuple findAll(List<String> id) {
    final var sql = new SqlStatement()
      .append("SELECT * ").ln()
      .append("  FROM ").append(options.getOrgActorStatus()).ln()
      .append("  WHERE ").ln();
    
    var index = 1;
    for(@SuppressWarnings("unused") final var arg : id) {
      if(index > 1) {
        sql.append(" OR ").ln();
      }
      sql.append(" (").append("id = $").append(index).append(")");
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
        .append("SELECT * FROM ").append(options.getOrgActorStatus())
        .build())
        .build();
  }
  @Override
  public SqlTuple getById(String id) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorStatus()).ln()
        .append("  WHERE (id = $1)").ln() 
        .build())
        .props(Tuple.of(id))
        .build();
  }
  @Override
  public SqlTuple findAllByIdRightId(String rightId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorStatus()).ln()
        .append("  WHERE (right_id = $1)").ln() 
        .build())
        .props(Tuple.of(rightId))
        .build();
  }
  @Override
  public SqlTuple findAllByMemberId(String memberId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorStatus()).ln()
        .append("  WHERE (member_id = $1)").ln() 
        .build())
        .props(Tuple.of(memberId))
        .build();
  }
  @Override
  public SqlTuple insertOne(OrgActorStatus doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgActorStatus())
        .append(" (id, commit_id, member_id, right_id, party_id, actor_status) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getMemberId(), doc.getRightId(), doc.getPartyId(), doc.getValue().name() }))
        .build();
  }
  @Override
  public SqlTuple updateOne(OrgActorStatus doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgActorStatus())
        .append(" SET actor_status = $1, commit_id = $2 ")
        .append(" WHERE id = $3")
        .build())
        .props(Tuple.from(new Object[]{doc.getValue().name(), doc.getCommitId(), doc.getId()}))
        .build();
  }
  @Override
  public SqlTupleList insertAll(Collection<OrgActorStatus> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgActorStatus())
        .append(" (id, commit_id, member_id, right_id, party_id, actor_status) VALUES($1, $2, $3, $4, $5, $6)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getMemberId(), doc.getRightId(), doc.getPartyId(), doc.getValue().name() }))
            .collect(Collectors.toList()))
        .build();
  }
  @Override
  public SqlTupleList updateMany(Collection<OrgActorStatus> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("UPDATE ").append(options.getOrgActorStatus())
        .append(" SET actor_status = $1, commit_id = $2 ")
        .append(" WHERE id = $3")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getValue().name(), doc.getCommitId(), doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTupleList deleteAll(Collection<OrgActorStatus> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getOrgActorStatus())
        .append(" WHERE id = $1")
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{doc.getId()}))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public SqlTuple findAllByPartyId(String partyId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgActorStatus()).ln()
        .append("  WHERE (party_id = $1)").ln() 
        .build())
        .props(Tuple.of(partyId))
        .build();
  }
  
  @Override
  public Function<Row, OrgActorStatus> defaultMapper() {
    return OrgActorStatusRegistrySqlImpl::orgActorStatus;
  }
  private static OrgActorStatus orgActorStatus(Row row) {
    final var actorStatus = row.getString("actor_status");
    return ImmutableOrgActorStatus.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .memberId(row.getString("member_id"))
        .rightId(row.getString("right_id"))
        .partyId(row.getString("party_id"))
        .value(actorStatus != null ? OrgActorStatus.OrgActorStatusType.valueOf(actorStatus) : null)
        .build();
  }

  @Override
  public Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgActorStatus()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  member_id VARCHAR(40),").ln()
    .append("  right_id VARCHAR(40),").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  actor_status VARCHAR(100) NOT NULL,").ln() // visibility: in_force | archived 
    .append("  UNIQUE NULLS NOT DISTINCT(member_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (commit_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (right_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_MEMBER_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (member_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgActorStatus()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgActorStatus()).append(" (party_id);").ln()
    

    .build()).build();
  }

  @Override
  public Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }

  @Override
  public Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgActorStatus()).append(";").ln()
        .build()).build();
  }


}
