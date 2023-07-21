package io.resys.thena.docdb.spi.pgsql;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.SqlMapper;
import io.resys.thena.docdb.sql.factories.SqlMapperImpl;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;


public class SqlMapperPg extends SqlMapperImpl implements SqlMapper {

  public SqlMapperPg(ClientCollections ctx) {
    super(ctx);
  }
  
  @Override
  public JsonObject jsonObject(Row row, String columnName) {
    return row.getJsonObject(columnName);
  }
}
