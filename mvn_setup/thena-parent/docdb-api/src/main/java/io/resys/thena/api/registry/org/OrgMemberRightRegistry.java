package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgMemberRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgMemberRightRegistry extends ThenaRegistryService<OrgMemberRight, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id); 
  SqlTuple findAllByUserId(String userId);
  SqlTuple findAllByRoleId(String userId);
  SqlTuple findAllByPartyId(String partyId);
  Sql findAll();
  SqlTuple findAll(List<String> id);
  SqlTuple insertOne(OrgMemberRight role);
  SqlTupleList insertAll(Collection<OrgMemberRight> roles);
  SqlTupleList deleteAll(Collection<OrgMemberRight> roles);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgMemberRight> defaultMapper();
}