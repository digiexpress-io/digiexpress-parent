package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.ThenaSqlClient;


public interface TagRegistry extends ThenaRegistryService<Tag, io.vertx.mutiny.sqlclient.Row> {
  ThenaSqlClient.SqlTuple getByName(String name);
  ThenaSqlClient.SqlTuple deleteByName(String name);
  ThenaSqlClient.Sql findAll();
  ThenaSqlClient.Sql getFirst();
  ThenaSqlClient.SqlTuple insertOne(Tag tag);
  
  ThenaSqlClient.Sql createTable();
  ThenaSqlClient.Sql createConstraints();
  ThenaSqlClient.Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Tag> defaultMapper();
}