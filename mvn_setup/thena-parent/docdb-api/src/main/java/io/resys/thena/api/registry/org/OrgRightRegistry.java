package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgRightRegistry extends ThenaRegistryService<OrgRight, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); //role name or id or external_id
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTuple findAll(Collection<String> id);
  ThenaSqlClient.SqlTuple insertOne(OrgRight role);
  ThenaSqlClient.SqlTuple findAllByPartyId(String partyId);
  ThenaSqlClient.SqlTuple findAllByMemberId(String memberId);
  
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgRight> roles);
  ThenaSqlClient.SqlTuple updateOne(OrgRight role);
  ThenaSqlClient.SqlTupleList updateMany(Collection<OrgRight> roles);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgRight> defaultMapper();

}