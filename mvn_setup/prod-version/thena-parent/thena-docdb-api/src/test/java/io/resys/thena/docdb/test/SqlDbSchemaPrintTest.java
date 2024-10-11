package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.defaults.DefaultSqlSchema;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlDbSchemaPrintTest {
  final InputStream expected_stream = SqlDbSchemaPrintTest.class.getClassLoader().getResourceAsStream("db.schema.sql");
  final Charset UTF_8 = StandardCharsets.UTF_8;
  @Test
  public void printSchema() throws IOException {
    final var sqlSchema = new DefaultSqlSchema(ClientCollections.defaults("public"));
    

    final var schema = new StringBuilder()
      .append(sqlSchema.blobs().getValue())
      .append(sqlSchema.commits().getValue())
      .append(sqlSchema.treeItems().getValue())
      .append(sqlSchema.trees().getValue())
      .append(sqlSchema.refs().getValue())
      .append(sqlSchema.tags().getValue())
      
      .append(sqlSchema.commitsConstraints().getValue())
      .append(sqlSchema.refsConstraints().getValue())
      .append(sqlSchema.tagsConstraints().getValue())
      .append(sqlSchema.treeItemsConstraints().getValue())
      .toString();
    
    
    final var actual = schema;    
    final var expected = new String(expected_stream.readAllBytes(), UTF_8);
    Assertions.assertEquals(expected, actual);
    
  }
}
