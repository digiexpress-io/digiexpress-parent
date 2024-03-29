package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgActorStatus;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgActorStatusRegistry extends ThenaRegistryService<OrgActorStatus, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id);
  ThenaSqlClient.SqlTuple findAllByIdRightId(String rightId);
  ThenaSqlClient.SqlTuple findAllByMemberId(String memberId);
  ThenaSqlClient.SqlTuple findAllByPartyId(String partyId);
  
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(List<String> id);
  ThenaSqlClient.SqlTuple insertOne(OrgActorStatus user);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgActorStatus> users);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgActorStatus> users);
  ThenaSqlClient.SqlTuple updateOne(OrgActorStatus user);
  ThenaSqlClient.SqlTupleList updateMany(Collection<OrgActorStatus> users);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgActorStatus> defaultMapper();
}