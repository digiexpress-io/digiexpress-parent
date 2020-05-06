package io.resys.hdes.datatype.spi.expressions.operations;

/*-
 * #%L
 * hdes-datatype
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import io.resys.hdes.datatype.api.DataTypeService.Operation;

public class LogicalOperation {
  
  
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Operation<Object> and(Operation ... values) {
    Collection<Operation> operations = Collections.unmodifiableList(Arrays.asList(values));
    return (Object parameter) -> {
        for(Operation value : operations) {
          if(!Boolean.TRUE.equals(value.apply(parameter))) {
            return false;
          }
        }
        return true;
    };
  }
  @SuppressWarnings({"rawtypes", "unchecked"})
  public static Operation<Object> not(Operation values) {
    return (Object parameter) -> !Boolean.TRUE.equals(values.apply(parameter));
  }
}
