package io.resys.thena.registry.org;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.org.ImmutableOrgMemberRight;
import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.registry.org.OrgMemberRightRegistry;
import io.resys.thena.datasource.ImmutableSql;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ImmutableSqlTupleList;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgMemberRightRegistrySqlImpl implements OrgMemberRightRegistry {
  private final TenantTableNames options;
  
  @Override
  public ThenaSqlClient.SqlTuple findAll(List<String> id) {
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
  public ThenaSqlClient.Sql findAll() {
    return ImmutableSql.builder()
        .value(new SqlStatement()
        .append("SELECT * FROM ").append(options.getOrgMemberRights())
        .build())
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple getById(String id) {
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
	public ThenaSqlClient.SqlTuple findAllByMemberId(String memberId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT member_rights.* ").ln()
        .append("  FROM ").append(options.getOrgMemberRights()).append(" as member_rights").ln()
        .append("  LEFT JOIN ").append(options.getOrgMembers()).append(" as member").ln()
        .append("  ON(member.id = member_rights.member_id)")
        .append("  WHERE member.id = $1 OR member.external_id = $1 OR member.username = $1").ln()
        .build())
        .props(Tuple.of(memberId))
        .build();
	}
	@Override
	public ThenaSqlClient.SqlTuple findAllByRoleId(String roleId) {
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
  public ThenaSqlClient.SqlTuple findAllByPartyId(String partyId) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("SELECT * ").ln()
        .append("  FROM ").append(options.getOrgMemberRights()).ln()
        .append("  WHERE party_id = $1").ln() 
        .build())
        .props(Tuple.of(partyId))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTuple insertOne(OrgMemberRight doc) {
    return ImmutableSqlTuple.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberRights())
        .append(" (id, commit_id, member_id, right_id, party_id) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getMemberId(), doc.getRightId(), doc.getPartyId()}))
        .build();
  }
  @Override
  public ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMemberRight> users) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("INSERT INTO ").append(options.getOrgMemberRights())
        .append(" (id, commit_id, member_id, right_id, party_id) VALUES($1, $2, $3, $4, $5)").ln()
        .build())
        .props(users.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId(), doc.getCommitId(), doc.getMemberId(), doc.getRightId(), doc.getPartyId()}))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgMemberRight> roles) {
    return ImmutableSqlTupleList.builder()
        .value(new SqlStatement()
        .append("DELETE FROM ").append(options.getOrgMemberRights())
        .append(" WHERE id = $1 ").ln()
        .build())
        .props(roles.stream()
            .map(doc -> Tuple.from(new Object[]{ doc.getId() }))
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public Function<Row, OrgMemberRight> defaultMapper() {
    return OrgMemberRightRegistrySqlImpl::orgMemberRight;
  }
  private static OrgMemberRight orgMemberRight(Row row) {
    return ImmutableOrgMemberRight.builder()
        .id(row.getString("id"))
        .commitId(row.getString("commit_id"))
        .rightId(row.getString("right_id"))
        .memberId(row.getString("member_id"))
        .partyId(row.getString("party_id"))
        .build();
  }

  @Override
  public ThenaSqlClient.Sql createTable() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getOrgMemberRights()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  commit_id VARCHAR(40) NOT NULL,").ln()
    .append("  member_id VARCHAR(40) NOT NULL,").ln()
    .append("  right_id VARCHAR(40) NOT NULL,").ln()
    .append("  party_id VARCHAR(40),").ln()
    .append("  UNIQUE NULLS NOT DISTINCT(member_id, right_id, party_id)").ln()
    .append(");").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_COMMIT_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (commit_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_RIGHT_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id);").ln()

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_MEMBER_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (member_id);").ln()
    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_PARTY_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (party_id);").ln()
    
    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, member_id);").ln()    

    .append("CREATE INDEX ").append(options.getOrgMemberRights()).append("_REF_2_INDEX")
    .append(" ON ").append(options.getOrgMemberRights()).append(" (right_id, member_id, party_id);").ln()    


    .build()).build();
  }

  @Override
  public ThenaSqlClient.Sql createConstraints() {
    return ImmutableSql.builder().value("").build();
  }

  @Override
  public ThenaSqlClient.Sql dropTable() {
    return ImmutableSql.builder().value(new SqlStatement()
        .append("DROP TABLE ").append(options.getOrgMemberRights()).append(";").ln()
        .build()).build();
  }

}
