package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface OrgMembershipRegistry extends ThenaRegistryService<OrgMembership, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.SqlTuple findAll(List<String> id);
  ThenaSqlClient.SqlTuple getById(String id); 
  ThenaSqlClient.SqlTuple findAllByGroupId(String groupId);
  ThenaSqlClient.SqlTuple findAllByUserId(String userId);
  ThenaSqlClient.SqlTuple insertOne(OrgMembership membership);
  ThenaSqlClient.SqlTupleList insertAll(Collection<OrgMembership> memberships);
  ThenaSqlClient.SqlTupleList deleteAll(Collection<OrgMembership> memberships);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMembership> defaultMapper();
  
}