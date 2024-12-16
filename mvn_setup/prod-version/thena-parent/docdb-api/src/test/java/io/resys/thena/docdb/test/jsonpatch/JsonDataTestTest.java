package io.resys.thena.docdb.test.jsonpatch;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.resys.thena.docdb.test.config.DbTestTemplate;
import io.resys.thena.jsonpatch.JsonPatch;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonDataTestTest {
  
  @Test
  public void RFC6901_sampleTest() throws IOException {
    final var testData = new JsonObject(DbTestTemplate.toString(JsonDataTestTest.class, "jsonpatch/RFC6901_sample.json"));
    final var sample = testData.getJsonObject("sample");
    final var emptyJsonObject = new JsonObject("{}");
    
    final var diff = JsonPatch.diff(emptyJsonObject, sample);
    log.debug("Created patch: \r\n{}", diff.getValue().encodePrettily());
    Assertions.assertEquals(testData.getJsonArray("expectedPatch").encodePrettily(), diff.getValue().encodePrettily());
    
    final var patchedObject = diff.apply(emptyJsonObject);
    log.debug("Applied patch: \r\n{}", patchedObject.encodePrettily());
    Assertions.assertEquals(sample.encodePrettily(), patchedObject.encodePrettily());
  }
  
  @Test
  public void randomData_createTest() throws IOException {
    final var sample = new JsonObject(DbTestTemplate.toString(JsonDataTestTest.class, "jsonpatch/randomData_create.json"));
    final var emptyJsonObject = new JsonObject("{}");
    final var diff = JsonPatch.diff(emptyJsonObject, sample);
    
    final var patchedObject = diff.apply(emptyJsonObject);
    Assertions.assertEquals(sample.encodePrettily(), patchedObject.encodePrettily());
  }
  
  @Test
  public void modifiedTest() throws IOException {
    final var sample_start = new JsonObject(DbTestTemplate.toString(JsonDataTestTest.class, "jsonpatch/randomData_create.json"));
    final var sample_end = new JsonObject(DbTestTemplate.toString(JsonDataTestTest.class, "jsonpatch/randomData_modified_1.json"));
    final var diff = JsonPatch.diff(sample_start, sample_end);
    log.debug("Created patch: \r\n{}", diff.getValue().encodePrettily());

    
    Assertions.assertEquals(
        new JsonArray(DbTestTemplate.toString(JsonDataTestTest.class, "jsonpatch/randomData_patch.json")).encodePrettily(), 
        diff.getValue().encodePrettily());
    
    
    final var patchedObject = diff.apply(sample_start);
    Assertions.assertEquals(sample_end.encodePrettily(), patchedObject.encodePrettily());
  }
}
