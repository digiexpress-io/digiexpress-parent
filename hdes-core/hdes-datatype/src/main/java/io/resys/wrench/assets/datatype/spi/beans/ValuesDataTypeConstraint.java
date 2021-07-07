package io.resys.wrench.assets.datatype.spi.beans;

/*-
 * #%L
 * wrench-assets-datatype
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import io.resys.wrench.assets.datatype.api.DataTypeRepository.ConstraintType;
import io.resys.wrench.assets.datatype.api.DataTypeRepository.DataTypeConstraint;

public class ValuesDataTypeConstraint implements DataTypeConstraint {

  private final List<String> values;

  public ValuesDataTypeConstraint(List<String> values) {
    super();
    this.values = values;
  }

  @Override
  public ConstraintType getType() {
    return ConstraintType.VALUES;
  }

  public List<String> getValues() {
    return values;
  }
}
