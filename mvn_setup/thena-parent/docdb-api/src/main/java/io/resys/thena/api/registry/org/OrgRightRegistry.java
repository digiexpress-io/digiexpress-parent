package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgRight;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgRightRegistry extends ThenaRegistryService<OrgRight, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id); //role name or id or external_id
  Sql findAll();
  SqlTuple findAll(Collection<String> id);
  SqlTuple insertOne(OrgRight role);
  SqlTupleList insertAll(Collection<OrgRight> roles);
  SqlTuple updateOne(OrgRight role);
  SqlTupleList updateMany(Collection<OrgRight> roles);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgRight> defaultMapper();
}