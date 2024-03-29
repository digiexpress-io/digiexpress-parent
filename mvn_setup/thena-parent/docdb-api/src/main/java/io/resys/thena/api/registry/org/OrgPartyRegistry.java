package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgPartyRegistry extends ThenaRegistryService<OrgParty, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); //group name or id or external_id
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(Collection<String> id);
  ThenaSqlClient.SqlTuple insertOne(OrgParty group);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgParty> OrgGroup);
  ThenaSqlClient.SqlTuple updateOne(OrgParty group);
  ThenaSqlClient.SqlTupleList updateMany(Collection<OrgParty> groups);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgParty> defaultMapper();
  
}