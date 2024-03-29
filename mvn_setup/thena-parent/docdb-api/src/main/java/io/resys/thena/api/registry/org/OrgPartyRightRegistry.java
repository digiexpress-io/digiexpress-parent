package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgPartyRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgPartyRightRegistry extends ThenaRegistryService<OrgPartyRight, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id); 
  SqlTuple findAllByGroupId(String groupId); 
  SqlTuple findAllByRoleId(String groupId);
  SqlTuple findAll(List<String> id);
  Sql findAll();
  
  SqlTuple insertOne(OrgPartyRight role);
  SqlTupleList insertAll(Collection<OrgPartyRight> roles);
  SqlTupleList deleteAll(Collection<OrgPartyRight> roles);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgPartyRight> defaultMapper();
}