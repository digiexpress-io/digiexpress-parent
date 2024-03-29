package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMembership;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgMembershipRegistry extends ThenaRegistryService<OrgMembership, io.vertx.mutiny.sqlclient.Row> {
  Sql findAll();
  SqlTuple findAll(List<String> id);
  SqlTuple getById(String id); 
  SqlTuple findAllByGroupId(String groupId);
  SqlTuple findAllByUserId(String userId);
  SqlTuple insertOne(OrgMembership membership);
  SqlTupleList insertAll(Collection<OrgMembership> memberships);
  SqlTupleList deleteAll(Collection<OrgMembership> memberships);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMembership> defaultMapper();
  
}