package io.resys.thena.fs.tests;

/*-
 * #%L
 * thena-docdb-api
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.github.vertical_blank.sqlformatter.languages.Dialect;

import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.fs.tables.spi.FsRegistry;
import io.resys.thena.fs.tables.spi.FsTableNames;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaSQL_Test {
  final InputStream expected_stream = SchemaSQL_Test.class.getClassLoader().getResourceAsStream("db.schema.sql");
  final Charset UTF_8 = StandardCharsets.UTF_8;
  @Test
  public void printSchema() throws Exception {

    final var tenant = ImmutableTenant.builder()
        .id("").name("").rev("").prefix("")
        .type(StructureType.fs)
        .build();
    
    final var ctx = TenantContext.defaults("").withTenant(tenant);
    final var datasource = new ThenaSqlDataSourceImpl(tenant, ctx, null, null, null, null); 
    final var names = FsTableNames.defaults().toRepo(tenant);
    final var registry = new FsRegistry(names, datasource);

    final var schema = SqlFormatter.of(Dialect.PostgreSql).format(new StringBuilder()
      .append(registry.nodes().createTable().getValue())
      .append(registry.blobs().createTable().getValue())
      .append(registry.props().createTable().getValue())
      .append(registry.trees().createTable().getValue())
      .append(registry.commits().createTable().getValue())
      .append(registry.refs().createTable().getValue())
      .append(registry.tags().createTable().getValue())

      .append(registry.nodes().createConstraints().getValue())
      .append(registry.trees().createConstraints().getValue())
      .toString()
    );

    log.debug("\r\n{}", schema);
    
    final var actual = IOUtils.readLines(new ByteArrayInputStream(schema.getBytes(UTF_8)), UTF_8);    
    final var expected = IOUtils.readLines(expected_stream, UTF_8);
    Assertions.assertLinesMatch(expected, actual, schema);
  }
}
