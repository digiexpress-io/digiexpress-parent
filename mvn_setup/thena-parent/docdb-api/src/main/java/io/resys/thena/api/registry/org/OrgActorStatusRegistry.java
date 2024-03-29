package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgActorStatusRegistry extends ThenaRegistryService<OrgActorStatus, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id);
  SqlTuple findAllByIdRightId(String rightId);
  SqlTuple findAllByMemberId(String memberId);
  SqlTuple findAllByPartyId(String partyId);
  
  Sql findAll();
  SqlTuple findAll(List<String> id);
  SqlTuple insertOne(OrgActorStatus user);
  SqlTupleList insertAll(Collection<OrgActorStatus> users);
  SqlTupleList deleteAll(Collection<OrgActorStatus> users);
  SqlTuple updateOne(OrgActorStatus user);
  SqlTupleList updateMany(Collection<OrgActorStatus> users);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgActorStatus> defaultMapper();
}