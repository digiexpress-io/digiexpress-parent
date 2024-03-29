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

import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.registry.doc.DocRegistrySqlImpl;
import io.resys.thena.registry.git.GitRegistrySqlImpl;
import io.resys.thena.storesql.SqlSchemaImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlDbSchemaPrintTest {
  final InputStream expected_stream = SqlDbSchemaPrintTest.class.getClassLoader().getResourceAsStream("db.schema.sql");
  final Charset UTF_8 = StandardCharsets.UTF_8;
  @Test
  public void printSchema() throws IOException {
    final var names = TenantTableNames.defaults("public");
    final var sqlSchema = new SqlSchemaImpl(names);
    final var git = new GitRegistrySqlImpl(names);
    final var doc = new DocRegistrySqlImpl(names);
    
    final var schema = new StringBuilder()
      .append(sqlSchema.createTenant().getValue())
      .append(git.blobs().createTable().getValue())
      .append(git.commits().createTable().getValue())
      .append(git.treeValues().createTable().getValue())
      .append(git.trees().createTable().getValue())
      .append(git.branches().createTable().getValue())
      .append(git.tags().createTable().getValue())
      .append(git.commits().createConstraints().getValue())
      .append(git.branches().createConstraints().getValue())
      .append(git.tags().createConstraints().getValue())
      .append(git.treeValues().createConstraints().getValue())
      
      
      .append(doc.docs().createTable().getValue())
      .append(doc.docBranches().createTable().getValue())
      .append(doc.docBranches().createConstraints().getValue())
      .append(doc.docCommits().createTable().getValue())
      .append(doc.docCommits().createConstraints().getValue())
      .append(doc.docLogs().createTable().getValue())
      .append(doc.docLogs().createConstraints().getValue())
      
      
      .append(sqlSchema.createOrgRights().getValue())
      .append(sqlSchema.createOrgParties().getValue())
      .append(sqlSchema.createOrgPartyRights().getValue())
      .append(sqlSchema.createOrgMembers().getValue())
      .append(sqlSchema.createOrgMemberRights().getValue())
      .append(sqlSchema.createOrgMemberships().getValue())
      .append(sqlSchema.createOrgActorStatus().getValue())
      .append(sqlSchema.createOrgCommits().getValue())
      .append(sqlSchema.createOrgActorData().getValue())
      .append(sqlSchema.createOrgRightsConstraints().getValue())
      .append(sqlSchema.createOrgMemberConstraints().getValue())
      .append(sqlSchema.createOrgPartyConstraints().getValue())
      .append(sqlSchema.createOrgCommitConstraints().getValue())
      
      .toString();
    
    log.debug(schema);
    
    final var actual = IOUtils.readLines(new ByteArrayInputStream(schema.getBytes(UTF_8)), UTF_8);    
    final var expected = IOUtils.readLines(expected_stream, UTF_8);
    Assertions.assertLinesMatch(expected, actual, schema);
    
  }
}
