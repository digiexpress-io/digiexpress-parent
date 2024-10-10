package io.thestencil.client.tests;

/*-
 * #%L
 * stencil-client
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

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.beans.SitesBean;
import io.thestencil.client.tests.util.PgProfile;
import io.thestencil.client.tests.util.PgTestTemplate;
import io.thestencil.client.tests.util.TestExporter;

@QuarkusTest
@TestProfile(PgProfile.class)
public class MigrationPgTest extends PgTestTemplate {

  @Test
  public void test1() throws JsonMappingException, JsonProcessingException {
    final var repo = getPersistence("migration-test");
 
    final var input = TestExporter.toString(getClass(), "migration-input.json");
    final SitesBean sites = PgTestTemplate.objectMapper.readValue(input, SitesBean.class);
    
    
    SiteState imported = repo.migration().importData(sites)
        .onFailure().invoke(e -> e.printStackTrace()).onFailure().recoverWithNull()
        .await().atMost(Duration.ofMinutes(1));

    
    String expected = TestExporter.toString(getClass(), "migration_state.txt");
    String actual = super.toRepoExport("migration-test");
    Assertions.assertEquals(expected, actual);
    
  }
}
