package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class AuthoringException extends RuntimeException {
  private static final long serialVersionUID = 7190168525508589141L;
  
  private final List<AuthoringModelProps> entity = new ArrayList<>();
  
  public AuthoringException(AuthoringModelProps entity, String msg) {
    super(msg(Arrays.asList(entity), msg));
    this.entity.add(entity);
  }
  
  public List<AuthoringModelProps> getEntity() {
    return entity;
  }
  
  private static String msg(List<AuthoringModelProps> entity, String msg) {
    StringBuilder messages = new StringBuilder()
      .append(System.lineSeparator())
      .append("  - ").append(msg);
    return new StringBuilder("Can't persist model").append(System.lineSeparator())
        .append("  errors: ").append(System.lineSeparator())
        .append(messages)
        .append("  model: ").append(System.lineSeparator())
        .append(entity.size() == 1 ? JsonObject.mapFrom(entity.getFirst()).encodePrettily() : new JsonArray(entity).encodePrettily())
        .toString();
  }
}
