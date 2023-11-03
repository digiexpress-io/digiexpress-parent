package io.resys.thena.docdb.test;

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.docdb.spi.ClientCollections;
import io.resys.thena.docdb.sql.factories.SqlSchemaImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlDbSchemaPrintTest {
  final InputStream expected_stream = SqlDbSchemaPrintTest.class.getClassLoader().getResourceAsStream("db.schema.sql");
  final Charset UTF_8 = StandardCharsets.UTF_8;
  @Test
  public void printSchema() throws IOException {
    final var sqlSchema = new SqlSchemaImpl(ClientCollections.defaults("public"));
    

    final var schema = new StringBuilder()
      .append(sqlSchema.createRepo().getValue())
      .append(sqlSchema.createBlobs().getValue())
      .append(sqlSchema.createCommits().getValue())
      .append(sqlSchema.createTreeItems().getValue())
      .append(sqlSchema.createTrees().getValue())
      .append(sqlSchema.createRefs().getValue())
      .append(sqlSchema.createTags().getValue())
      
      .append(sqlSchema.createCommitsConstraints().getValue())
      .append(sqlSchema.createRefsConstraints().getValue())
      .append(sqlSchema.createTagsConstraints().getValue())
      .append(sqlSchema.createTreeItemsConstraints().getValue())
      .toString();
    
    log.debug(schema);
    
    final var actual = IOUtils.readLines(new ByteArrayInputStream(schema.getBytes(UTF_8)), UTF_8);    
    final var expected = IOUtils.readLines(expected_stream, UTF_8);
    Assertions.assertLinesMatch(expected, actual);
    
  }
}
