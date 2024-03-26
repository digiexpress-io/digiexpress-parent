package io.resys.thena.storefile.tables;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.List;
import java.util.function.Function;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.spi.DataMapper;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storefile.tables.Table.Row;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Tuple;

public interface Table<T extends Row>  {
  String getTableName();
  Boolean getExists();
  T insert(T entry);
  List<T> insertAll(List<T> entry);
  T delete(T entry);
  boolean create();
  List<T> getRows();
  
  interface Row { 
    @SuppressWarnings("unchecked")
    default <T extends Row> T toType(Class<T> type) {
      return (T) this;
    }
  }
  
  interface RowExists extends Row { boolean getExists(); }

  
  interface FileMapper extends DataMapper<Table.Row> { }
  interface Connection {
    RepoTable getRepoTable(DbCollections ctx);
  }
  
  interface FilePool {
    FilePreparedQuery<Object> preparedQuery(FileStatement query);
    FilePreparedQuery<Object> preparedQuery(FileTuple query);
    FilePreparedQuery<Object> preparedQuery(FileTupleList query);
  }
  
  interface FilePreparedQuery<T> {
    <U> FilePreparedQuery<U> mapping(Function<Row, U> mapper);
    Uni<List<T>> execute();
  }
  
  @Value.Immutable
  interface FileClientWrapper {
    Tenant getRepo();
    FilePool getClient();
    DbCollections getNames();    
  }
  
  interface FileCommand {
    String getValue();
    Function<Connection, List<? extends Row>> getCommand();    
  }

  @Value.Immutable
  interface FileStatement extends FileCommand { }
  
  @Value.Immutable
  interface FileTuple extends FileCommand {
    Tuple getProps();
  }
  @Value.Immutable
  interface FileTupleList extends FileCommand {
    List<Tuple> getProps();
  }
}
