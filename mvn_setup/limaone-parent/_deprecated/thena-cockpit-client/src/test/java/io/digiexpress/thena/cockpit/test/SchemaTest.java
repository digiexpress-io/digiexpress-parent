package io.digiexpress.thena.cockpit.test;

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

import io.digiexpress.thena.cockpit.client.tables.spi.CockpitRegistry;
import io.digiexpress.thena.cockpit.client.tables.spi.CockpitTableNames;
import io.resys.thena.api.entities.ImmutableTenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SchemaTest {
  final InputStream expected_stream = SchemaTest.class.getClassLoader().getResourceAsStream("db.schema.sql");
  final Charset UTF_8 = StandardCharsets.UTF_8;
  @Test
  public void printSchema() throws Exception {

    final var tenant = ImmutableTenant.builder()
        .id("").name("").rev("").prefix("")
        .type(StructureType.cockpit)
        .build();
    
    final var ctx = TenantContext.defaults("").withTenant(tenant);
    final var datasource = new ThenaSqlDataSourceImpl(tenant, ctx, null, null, null, null); 
    final var names = CockpitTableNames.defaults().toRepo(tenant);
    final var registry = new CockpitRegistry(names, datasource);

    
      final var schema = SqlFormatter.of(Dialect.PostgreSql).format(new StringBuilder()
          .append(registry.cockpitConfigs().createTable().getValue())
          .append(registry.cockpitConfigTenants().createTable().getValue())
          .append(registry.cockpitConfigProps().createTable().getValue())
          .append(registry.cockpitCommits().createTable().getValue())
          .append(registry.cockpitCommitTrees().createTable().getValue())

          .append(registry.cockpitConfigs().createConstraints().getValue())
          .append(registry.cockpitConfigTenants().createConstraints().getValue())
          .append(registry.cockpitConfigProps().createConstraints().getValue())
          .append(registry.cockpitCommits().createConstraints().getValue())
          .append(registry.cockpitCommitTrees().createConstraints().getValue())
          .toString()
        );

    log.debug("\r\n{}", schema);
    
    final var actual = IOUtils.readLines(new ByteArrayInputStream(schema.getBytes(UTF_8)), UTF_8);    
    final var expected = IOUtils.readLines(expected_stream, UTF_8);
    Assertions.assertLinesMatch(expected, actual, schema);
    
  }
}
