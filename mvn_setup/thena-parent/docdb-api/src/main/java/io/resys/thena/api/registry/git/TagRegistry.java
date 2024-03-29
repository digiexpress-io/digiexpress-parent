package io.resys.thena.api.registry.git;

import java.util.function.Function;

import io.resys.thena.api.entities.git.Tag;
import io.resys.thena.api.registry.ThenaRegistryService;
import io.resys.thena.datasource.SqlQueryBuilder.Sql;
import io.resys.thena.datasource.SqlQueryBuilder.SqlTuple;


public interface TagRegistry extends ThenaRegistryService<Tag, io.vertx.mutiny.sqlclient.Row> {
  SqlTuple getByName(String name);
  SqlTuple deleteByName(String name);
  Sql findAll();
  Sql getFirst();
  SqlTuple insertOne(Tag tag);
  
  Sql createTable();
  Sql createConstraints();
  Sql dropTable();
  
  Function<io.vertx.mutiny.sqlclient.Row, Tag> defaultMapper();
}