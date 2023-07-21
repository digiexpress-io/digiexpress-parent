package io.resys.thena.docdb.sql.support;

import java.util.Optional;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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


import org.immutables.value.Value;

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.spi.ClientCollections;

@Value.Immutable
public interface SqlClientWrapper {
  Repo getRepo();
  io.vertx.mutiny.sqlclient.Pool getPool();
  Optional<io.vertx.mutiny.sqlclient.SqlClient> getTx();
  ClientCollections getNames();
  
  default io.vertx.mutiny.sqlclient.SqlClient getClient() {
    return getTx().orElse(getPool());
  }
}
