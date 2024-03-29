package io.resys.thena.api.registry.org;

import java.util.function.Function;

import io.resys.thena.api.entities.org.OrgActorData;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;


public interface OrgActorDataRegistry extends ThenaRegistryService<OrgActorData, io.vertx.mutiny.sqlclient.Row> {
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, OrgActorData> defaultMapper();
}