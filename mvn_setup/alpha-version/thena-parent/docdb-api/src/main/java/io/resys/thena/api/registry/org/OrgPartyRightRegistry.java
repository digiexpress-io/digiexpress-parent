package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgPartyRightRegistry extends ThenaRegistryService<OrgPartyRight, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); 
  ThenaSqlClient.SqlTuple findAllByPartyId(String groupId); 
  ThenaSqlClient.SqlTuple findAllByRoleId(String groupId);
  ThenaSqlClient.SqlTuple findAll(List<String> id);
  ThenaSqlClient.Sql findAll();
  
  ThenaSqlClient.SqlTuple insertOne(OrgPartyRight role);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgPartyRight> roles);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgPartyRight> roles);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgPartyRight> defaultMapper();
}