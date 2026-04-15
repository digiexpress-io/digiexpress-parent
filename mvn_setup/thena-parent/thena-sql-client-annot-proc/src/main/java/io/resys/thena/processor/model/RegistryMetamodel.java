package io.resys.thena.processor.model;

/*-
 * #%L
 * thena-sql-client-annot-proc
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

import java.util.List;

import javax.lang.model.element.Element;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RegistryMetamodel {
  Element element;
  String name;                    // "Grim"
  String packageName;             // "io.resys.thena.grim.spi.sql"
  String tableClassName;
  String registryClassName;
  String internalTenantQueryClassName;
  String transactionContainerClassName;
  String transactionSaveClassName;
  String worldName;
  String tenantType;
  boolean isTenantDisabled;
  @Builder.Default
  List<String> nonTenantTables = List.of();  // ["process", "process_id_seq"]
  
  
  public String getExceptionClassName() {
    final var exceptionClassName = name + "BuilderException";
    return exceptionClassName;
  }
}
