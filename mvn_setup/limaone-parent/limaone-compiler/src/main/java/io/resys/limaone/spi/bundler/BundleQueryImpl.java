package io.resys.limaone.spi.bundler;

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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.program.Compiler.BundleQuery;
import io.resys.limaone.spi.bundler.BundleGroup.ProgramQueryException;
import io.vertx.core.json.JsonObject;
import io.resys.limaone.program.Program;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BundleQueryImpl<T extends Program> implements BundleQuery<T> {

  private final BundleGroup<T> programs;
  private String name;
  private String externalId;
  private String id;
  private String locale;
  
  @Override
  public BundleQuery<T> name(String name) {
    this.name = Objects.requireNonNull(name, () -> "name must be defined!");
    return this;
  }
  @Override
  public BundleQuery<T> externalId(String externalId) {
    this.externalId = Objects.requireNonNull(externalId, () -> "externalId must be defined!");
    return this;
  }
  @Override
  public BundleQuery<T> id(String id) {
    this.id = Objects.requireNonNull(id, () -> "id must be defined!");
    return this;
  }
  @Override
  public BundleQuery<T> locale(String locale) {
    this.locale = Objects.requireNonNull(locale, () -> "locale must be defined!");
    return this;
  }
  @Override
  public Optional<T> findOne() {
    Optional<T> result = Optional.empty();
    if(id != null) {
      result = programs.findOne(id);
    }
    if(result.isPresent()) {
      return result;
    }
    
    if(name != null) {
      result = programs.findOne(name);
    }
    if(result.isPresent()) {
      return result;
    }
    
    if(externalId != null) {
      result = programs.findOne(externalId);
    }
    if(result.isPresent()) {
      return result;
    }
    
    
    return Optional.empty();
  }
  @Override
  public T getOne() {
    return findOne().orElseThrow(() -> new ProgramQueryException(
        "Query#findOne failed, expected: 1, actual: 0 programs, predicate: " + 
        JsonObject.of(
          "id", id, 
          "name", name,
          "externalId", externalId
        ).encodePrettily()));
  }
  @Override
  public void forEach(Consumer<T> t) {
    
    final var isFilterDisabled = (
        locale == null &&
        id == null &&
        name == null &&
        externalId == null
    );

    programs.getProgramById().values()
      .stream()
      .filter(program -> {
        
        if(isFilterDisabled) {
          return true;
        }
        
        if(locale != null && program.getLocales().size() > 0 &&
            !program.getLocales().stream().filter(l -> l.equalsIgnoreCase(locale)).findFirst().isEmpty()) {
          return false;
        }
        
        if(id != null && program.getId().equals(id)) {
          return true;
        }
        if(name != null && program.getName().equals(name)) {
          return true;
        }
        if(externalId != null && (program.getId().equals(externalId) || program.getName().equals(externalId))) {
          return true;
        }
        
        return false;
      })
      .forEach(t);
  }
}
