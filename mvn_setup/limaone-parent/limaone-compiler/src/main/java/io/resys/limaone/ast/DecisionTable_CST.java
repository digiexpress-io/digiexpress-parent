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
import java.util.Collection;
import java.util.Map;

import org.immutables.value.Value;

import io.resys.limaone.ast.Yaml_CST.Yaml;
import jakarta.annotation.Nullable;

public interface DecisionTable_CST extends Serializable {

  @Value.Immutable
  interface YamlValueSetType extends DecisionTable_CST {
    String getName();
    Collection<String> getValues();
  }

  interface YamlDecision extends Yaml {
    Yaml getName();
    Yaml getDescription();
    Yaml getHitPolicy();
    Map<String, YamlValueSet> getValueSetNodes();
    YamlTable getTable();
  }

  interface YamlValueSet extends Yaml {
    String getName();
    Collection<String> getValues();
  }

  interface YamlTable extends Yaml {
    String getMarkdownContent();
    Collection<YamlTableHeader> getHeaders();
    Collection<YamlTableRow> getRows();
    Map<String, YamlTableHeader> getInputHeaders();
    Map<String, YamlTableHeader> getOutputHeaders();
  }

  interface YamlTableHeader extends Yaml {
    String getName();
    String getType();
    boolean isOutput();
    int getColumnIndex();
  }

  interface YamlTableRow extends Yaml {
    int getRowIndex();
    Collection<YamlTableCell> getCells();
    Map<String, YamlTableCell> getCellsByHeader();
  }

  interface YamlTableCell extends Yaml {
    String getHeaderName();
    String getExpression();
    int getColumnIndex();
    int getRowIndex();
    @Nullable String getValue();
  }
}
