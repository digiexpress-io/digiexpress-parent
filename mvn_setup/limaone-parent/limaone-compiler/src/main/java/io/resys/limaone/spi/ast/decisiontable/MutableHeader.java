package io.resys.limaone.spi.ast.decisiontable;

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
import java.util.List;

import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;

public class MutableHeader implements Comparable<MutableHeader> {

  private final String id;
  private Direction direction;

  private String script;
  private String name;
  private String extRef;
  private ValueType value;
  int order;
  final List<MutableCell> cells = new ArrayList<>();
  private List<String> valueSet = new ArrayList<>();

  public MutableHeader(String id, Direction direction, int order) {
    super();
    this.id = id;
    this.direction = direction;
    this.order = order;
  }
  public String getScript() {
    return script;
  }
  public void setScript(String script) {
    this.script = script;
  }
  public String getName() {
    return name;
  }
  public MutableHeader setName(String name) {
    this.name = name;
    return this;
  }
  public String getExtRef() {
    return extRef;
  }
  public MutableHeader setExtRef(String extRef) {
    this.extRef = extRef;
    return this;
  }
  public ValueType getValue() {
    return value;
  }
  public MutableHeader setValue(ValueType value) {
    this.value = value;
    return this;
  }
  public int getOrder() {
    return order;
  }
  public MutableHeader setOrder(int order) {
    this.order = order;
    return this;
  }
  public List<MutableCell> getCells() {
    return cells;
  }
  public MutableCell getRowCell(String rowId) {
    return cells.stream().filter(c -> c.getRow().equals(rowId)).findFirst().get();
  }
  public String getId() {
    return id;
  }
  public Direction getDirection() {
    return direction;
  }
  public List<String> getValueSet() {
    return valueSet;
  }
  public MutableHeader setValueSet(List<String> values) {
    this.valueSet = values;
    return this;
  }
  @Override
  public int compareTo(MutableHeader o) {
    int d0 = direction == Direction.IN ? 0 : 1;
    int d1 = o.getDirection() == Direction.IN ? 0 : 1;

    int direction = Integer.compare(d0, d1);
    if(direction == 0) {
      return Integer.compare(order, o.order);
    }
    return direction;
  }
  public MutableHeader setDirection(Direction direction) {
    this.direction = direction;
    return this;
  }
}
