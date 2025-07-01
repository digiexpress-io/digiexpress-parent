package io.digiexpress.eveli.client.config;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EveliAutoConfigTest {
  @Test
  void testEveliAutoConfigDatabaseUrlParsing() {
    EveliAutoConfig config = new EveliAutoConfig();
    var datasourcePassword = "password123";
    var datasourceUsername = "username";
    var props = config.eveliPropsDbResolved("jdbc:postgresql://public-postgres.database.com:12343/defaultdb?sslmode=require&sslrootcert=demo/database-ca.pem", datasourceUsername, datasourcePassword);
    assertNotNull(props);
    assertEquals("password123", props.getPassword());
    assertEquals("username", props.getUsername());
    assertEquals("public-postgres.database.com", props.getHost());
    assertEquals(12343, props.getPort());
    assertEquals("defaultdb", props.getDatabase());
    assertEquals("require", props.getSslMode());
    assertEquals(false, props.getSslTrustAll());
    assertEquals("demo/database-ca.pem", props.getCertPath());

    props = config.eveliPropsDbResolved("jdbc:postgresql://public-postgres.database.com:12343/defaultdb123?sslmode=verify-full&ssltrustall=true", datasourceUsername, datasourcePassword);
    assertEquals("public-postgres.database.com", props.getHost());
    assertEquals(12343, props.getPort());
    assertEquals("defaultdb123", props.getDatabase());
    assertEquals("verify-full", props.getSslMode());
    assertEquals(true, props.getSslTrustAll());
    assertNull(props.getCertPath());

    props = config.eveliPropsDbResolved("jdbc:postgresql://public-postgres.database.com:12343/defaultdb123?", datasourceUsername, datasourcePassword);
    assertEquals("public-postgres.database.com", props.getHost());
    assertEquals(12343, props.getPort());
    assertEquals("defaultdb123", props.getDatabase());
    assertEquals("allow", props.getSslMode());
    assertEquals(false, props.getSslTrustAll());
    assertNull(props.getCertPath());

    props = config.eveliPropsDbResolved("jdbc:postgresql://public-postgres.database.com/defaultdb123", datasourceUsername, datasourcePassword);
    assertEquals("public-postgres.database.com", props.getHost());
    assertEquals(5432, props.getPort());
    assertEquals("defaultdb123", props.getDatabase());
    assertEquals("allow", props.getSslMode());
    assertEquals(false, props.getSslTrustAll());
    assertNull(props.getCertPath());
  }
}
