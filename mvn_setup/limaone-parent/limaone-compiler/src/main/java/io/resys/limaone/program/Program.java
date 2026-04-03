package io.resys.limaone.program;

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

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;

public interface Program extends Serializable {
  
  String getId();
  String getName();
  Model.BodyType getType();
  ProgramStatus getStatus();

  // locale constrained program, empty for no constraint
  List<String> getLocales();
  
  List<Parameter> getHeaders();
  List<ModelError> getErrors();
  List<ProgramAssociation> getAssociations();
  
  interface ProgramResult extends Serializable {}
  
  interface ProgramLog extends Serializable {}
  
  @Value.Immutable
  interface ProgramAssociation {
    Optional<String> getId();
    String getRef();
    Model.BodyType  getRefType();
    ProgramStatus getRefStatus();
    Boolean getOwner();
  }

  enum ProgramStatus { UP, ERROR }

}
