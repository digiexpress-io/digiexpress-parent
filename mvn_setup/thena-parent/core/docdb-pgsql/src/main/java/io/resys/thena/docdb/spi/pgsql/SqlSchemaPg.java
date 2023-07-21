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
import io.resys.thena.docdb.sql.ImmutableSql;
import io.resys.thena.docdb.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import io.resys.thena.docdb.sql.support.SqlStatement;

public class SqlSchemaPg extends SqlSchemaImpl {
  public SqlSchemaPg(ClientCollections options) {
    super(options);
  }

  @Override
  public Sql blobs() {
    return ImmutableSql.builder().value(new SqlStatement().ln()
    .append("CREATE TABLE ").append(options.getBlobs()).ln()
    .append("(").ln()
    .append("  id VARCHAR(40) PRIMARY KEY,").ln()
    .append("  value jsonb NOT NULL").ln()
    .append(");").ln()
    .build()).build();
  }
  
  @Override
  public SqlSchemaImpl withOptions(ClientCollections options) {
    return new SqlSchemaPg(options);
  }
}
