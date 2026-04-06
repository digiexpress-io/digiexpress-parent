package io.resys.limaone.ast;

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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter;



public interface CSV_AST {

  List<Parameter> getHeaders();
  List<CSVRow> getRows();
  
  // concat back to csv, gives key-values and expects 1-* results
  String forEach(Function<Map<String, Serializable>, List<Map<String, Serializable>>> mapper);

  @Value.Immutable
  interface CSVRow {
    int getRowIndex();
    List<CSVCell> getCells();
    
    default Map<String, Serializable> toMap() {
      return this.getCells().stream()
          .collect(Collectors.toMap(
              e -> e.getParameterName(), 
              e -> e.getParameterValue()
          ));
    }
  }
  
  @Value.Immutable
  interface CSVCell {
    String getParameterName();
    Serializable getParameterValue();
  }
}
