package io.digiexpress.eveli.client.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Disabled;

/*-
 * #%L
 * eveli-client
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

import org.junit.jupiter.api.Test;

import io.digiexpress.eveli.client.migration.BuildMigrationAssets;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AssetsToVersion_2 {

  @Test
  @Disabled
  public void readAssets() throws IOException {
    
    new File("src/test/resources/migrated").mkdir();

    
    final var deployment = new BuildMigrationAssets().build();
    
    final var writer = new BufferedWriter(new FileWriter("src/test/resources/migrated/deployment-" + LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) + ".json", true));
    BuildMigrationAssets.OBJECT_MAPPER.writeValue(writer, deployment);
    writer.close();
    
  }
}
