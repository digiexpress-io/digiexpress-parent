package io.resys.thena.api.registry.org;

import java.util.Collection;
import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgParty;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTupleList;


public interface OrgPartyRegistry extends ThenaRegistryService<OrgParty, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getById(String id); //group name or id or external_id
  Sql findAll();
  SqlTuple findAll(Collection<String> id);
  SqlTuple insertOne(OrgParty group);
  SqlTupleList insertAll(Collection<OrgParty> OrgGroup);
  SqlTuple updateOne(OrgParty group);
  SqlTupleList updateMany(Collection<OrgParty> groups);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgParty> defaultMapper();
  
}