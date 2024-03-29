package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgMemberRightRegistry extends ThenaRegistryService<OrgMemberRight, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getById(String id); 
  ThenaSqlClient.SqlTuple findAllByUserId(String userId);
  ThenaSqlClient.SqlTuple findAllByRoleId(String userId);
  ThenaSqlClient.SqlTuple findAllByPartyId(String partyId);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(List<String> id);
  ThenaSqlClient.SqlTuple insertOne(OrgMemberRight role);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMemberRight> roles);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgMemberRight> roles);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberRight> defaultMapper();
}