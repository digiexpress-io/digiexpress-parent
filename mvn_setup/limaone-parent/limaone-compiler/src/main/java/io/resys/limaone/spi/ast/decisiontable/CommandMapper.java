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

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.resys.limaone.ast.DecisionTable_AST.DecisionRowNode;
import io.resys.limaone.ast.ImmutableDecisionCellNode;
import io.resys.limaone.ast.ImmutableDecisionRowNode;
import io.resys.limaone.ast.ImmutableDecisionTable_AST;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.model.DecisionTable.ColumnExpressionType;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Parameter.Direction;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.ExpressionProgram;
import io.resys.limaone.spi.ast.AST_Exception;
import io.resys.limaone.spi.compiler.Compiler_Expression;
import io.resys.limaone.spi.parameter.Parameter_Factory;

public class CommandMapper {

  private final IdFixer idGen = new IdFixer();

  private String name;
  private String description;
  private HitPolicy hitPolicy;


  private final static List<String> headerTypes = Collections.unmodifiableList(
      Arrays.asList(
          ValueType.STRING,
          ValueType.BOOLEAN, 
          ValueType.INTEGER, 
          ValueType.LONG, 
          ValueType.DECIMAL, 
          ValueType.DATE, 
          ValueType.DATE_TIME,
          ValueType.INTL).stream()
      .map(v -> v.name()).collect(Collectors.toList()));

  private final static Map<ValueType, List<String>> headerExpressions = Collections.unmodifiableMap(Map.of(
      ValueType.INTEGER, Collections.unmodifiableList(Arrays.asList(ColumnExpressionType.EQUALS.name())),
      ValueType.DECIMAL, Collections.unmodifiableList(Arrays.asList(ColumnExpressionType.EQUALS.name())),
      ValueType.STRING, Collections.unmodifiableList(Arrays.asList(ColumnExpressionType.IN.name()))    
      ));
  private final static List<String> dynamocValueExpressions = Collections.unmodifiableList(Arrays.asList("<=", "<",">=", ">", "="));

  private static Object parseVariable(String expression, ValueType type) {
    Optional<String> comparison = dynamocValueExpressions.stream().filter(v -> expression.startsWith(v)).findFirst();
    if(!comparison.isPresent()) {
      switch(type) {
        case DECIMAL:
          return BigDecimal.ZERO;
        case LONG:
          return 0;
        case INTEGER:
          return 0;
        default: return null;
      }
    }
    String value = expression.substring(comparison.get().length()).trim();
    switch(type) {
      case DECIMAL:
        return new BigDecimal(value);
      case LONG:
        return Long.parseLong(value);
      case INTEGER:
        return Integer.parseInt(value);
      default: return null;
    }
  }

  private ValueType getValueType(MutableHeader header) {
    return header.getValue();
  }
  public CommandMapper name(String name) {
    this.name = name;
    return this;
  }
  public CommandMapper description(String description) {
    this.description = description;
    return this;
  }
  public CommandMapper hitPolicy(HitPolicy hitPolicy) {
    this.hitPolicy = hitPolicy;
    return this;
  }
  public Map.Entry<String, CommandMapper> addHeader(Direction direction, String name) {
    MutableHeader header = idGen.addHeader()
        .setDirection(direction)
        .setName(name)
        .setValue(ValueType.STRING);
    return new AbstractMap.SimpleImmutableEntry<String, CommandMapper>(header.getId(), this);
  }
  public CommandMapper changeHeaderType(String id, String value) {
    try {
      idGen.getHeader(id).setValue(ValueType.valueOf(value));
    } catch(Exception e) {
      idGen.getHeader(id).setValue(null);
    }

    return this;
  }
  public CommandMapper changeHeaderScript(String id, String value) {
    idGen.getHeader(id).setScript(value);
    return this;
  }
  public CommandMapper changeHeaderName(String id, String value) {
    idGen.getHeader(id).setName(value);
    return this;
  }
  public CommandMapper changeHeaderExtRef(String id, String value) {
    idGen.getHeader(id).setExtRef(value);
    return this;
  }
  public CommandMapper changeHeaderDirection(String id, Direction value) {
    MutableHeader header = idGen.getHeader(id).setDirection(value);
    ValueType valueType = getValueType(header);

    // Remove expression if cell new direction is out
    if(value == Direction.OUT && valueType != null) {
      header.getCells().stream()
      .filter(c -> !StringUtils.isEmpty(c.getValue()))
      .forEach(cell -> {

        try {
          final var expression = Compiler_Expression.build(cell.getValue(), valueType);
          if(expression.getConstants().size() == 1) {
            cell.setValue(expression.getConstants().get(0));
          }
        } catch(AST_Exception e) {
          cell.setValue(cell.getValue());
        }
      });
    }

    return this;
  }

  private String getExpression(ValueType valueType, ColumnExpressionType value, String columnValue) {
    String constant;
    try {
      final ExpressionProgram expression = Compiler_Expression.build(columnValue, valueType);
      if(expression.getConstants().size() != 1) {
        return null;
      }
      constant = expression.getConstants().get(0);
    } catch(AST_Exception e) {
      constant = columnValue.trim();
    }
    switch (value) {
      case EQUALS:
        return "= " + constant;
      case IN:
        return "in[\"" + constant + "\"]";
      case QIN:
        return "qin[\"" + constant + "\"]";
      default:
        return null;
    }
  }

  public CommandMapper setHeaderExpression(String id, ColumnExpressionType value) {
    MutableHeader header = idGen.getHeader(id);
    ValueType valueType = getValueType(header);

    if(header.getDirection() == Direction.IN && valueType != null) {
      header.cells.stream()
      .filter(c -> !StringUtils.isEmpty(c.getValue()))
      .forEach(cell -> {
        String operation = getExpression(valueType, value, cell.getValue());
        if(operation != null) {
          cell.setValue(operation);
        }
      });
    }

    return this;
  }
  public CommandMapper changeCell(String id, String value) {
    idGen.getCell(id).setValue(value);
    return this;
  }
  public CommandMapper changeCell(String rowId, int columnIndex, String value) {
    MutableHeader column = idGen.getHeaders().values().stream().filter(r -> r.order == columnIndex).findFirst().get();
    MutableCell cell = column.getCells().stream().filter(c -> c.getRow().equals(rowId)).findFirst().get();
    cell.setValue(value);
    return this;
  }
  public CommandMapper deleteCell(String id) {
    idGen.getCell(id).setValue(null);
    return this;
  }
  public CommandMapper deleteHeader(String id) {
    idGen.deleteHeader(id);
    return this;
  }
  public String addRow() {
    final var row = idGen.addRow();
    return row.getId();
  }
  public CommandMapper deleteRow(String id) {
    idGen.deleteRow(id);
    return this;
  }
  public CommandMapper deleteRows() {
    new ArrayList<>(idGen.getRows().keySet()).forEach(id -> deleteRow(id));
    return this;
  }
  public CommandMapper deleteColumns() {
    new ArrayList<>(idGen.getHeaders().keySet()).forEach(id -> deleteHeader(id));
    return this;
  }
  public CommandMapper moveRow(String srcId, String targetId) {
    MutableRow src = idGen.getRow(srcId);
    MutableRow target = idGen.getRow(targetId);

    int targetOrder = src.getOrder();
    int srcOrder = target.getOrder();
    src.setOrder(srcOrder);
    target.setOrder(targetOrder);
    return this;
  }
  public CommandMapper insertRow(String srcId, String targetId) {
    MutableRow src = idGen.getRow(srcId);
    MutableRow target = idGen.getRow(targetId);

    // move row from back to front
    if(src.getOrder() > target.getOrder()) {
      int start = target.getOrder();
      int end = src.getOrder();

      for(MutableRow row : this.idGen.getRows().values()) {
        if(row.getOrder() >= start && row.getOrder() < end) {
          row.setOrder(row.getOrder() + 1);
        }
      }
      src.setOrder(start);
    } else {
      // move row from front to back
      int start = src.getOrder();
      int end = target.getOrder();

      for(MutableRow row : this.idGen.getRows().values()) {
        if(row.getOrder() > start && row.getOrder() <= end) {
          row.setOrder(row.getOrder() - 1);
        }
      }

      src.setOrder(end);
    }
    return this;
  }

  public CommandMapper copyRow(String srcId) {
    MutableRow src = idGen.getRow(srcId);
    String targetId = addRow();

    for(MutableHeader header : this.idGen.getHeaders().values()) {
      MutableCell from = header.getCells().stream().filter(c -> c.getRow().equals(src.getId())).findFirst().get();
      MutableCell to = header.getCells().stream().filter(c -> c.getRow().equals(targetId)).findFirst().get();
      to.setValue(from.getValue());
    }

    return insertRow(targetId, srcId);
  }

  public CommandMapper moveHeader(String srcId, String targetId) {
    MutableHeader src = idGen.getHeader(srcId);
    MutableHeader target = idGen.getHeader(targetId);

    int targetOrder = src.getOrder();
    int srcOrder = target.getOrder();

    Direction targetDirection = src.getDirection();
    Direction srcDirection = target.getDirection();

    src.setOrder(srcOrder).setDirection(srcDirection);
    target.setOrder(targetOrder).setDirection(targetDirection);
    return this;
  }

  public CommandMapper setValueSet(String id, String values) {
    if (values.length() > 0) {
      List<String> valueList = Arrays.asList(values.split(", "));
      idGen.getHeader(id).setValueSet(valueList);
      return this;
    }
    idGen.getHeader(id).setValueSet(new ArrayList<>());
    return this;
  }

  private String resolveScriptValue(MutableHeader header, MutableCell cell) {
    final Map<String, Object> context = new HashMap<>();
    for(MutableHeader h : idGen.getHeaders().values()) {
      MutableCell value = h.getCells().stream()
          .filter(c -> c.getRow().equals(cell.getRow()))
          .findFirst().get();
      try {
        final Object variable = parseVariable(value.getValue(), h.getValue());
        context.put(h.getName(), variable);
      } catch(Exception e) {
      }
    }

    try {
      return Compiler_Expression.build(header.getScript(), ValueType.MAP).run(context) + "";
    } catch(Exception e) {
      return null;
    }
  }

  public ImmutableDecisionTable_AST.Builder build() {
    this.idGen.getHeaders().values().stream()
      .filter(h -> !StringUtils.isEmpty(h.getScript()))
      .forEach(h -> h.getCells().forEach(c -> c.setValue(resolveScriptValue(h, c))));

    final var headers = this.idGen.getHeaders().values().stream().sorted()
        .map(h -> Parameter_Factory.newParam()
            .direction(h.getDirection())
            .name(h.getName())
            .valueType(h.getValue())
            .id(h.getId())
            .order(h.getOrder())
            .valueSet(h.getValueSet())
            .script(h.getScript())
            .extRef(h.getExtRef())
            .build())
        .collect(Collectors.toList());

    final List<DecisionRowNode> rows = this.idGen.getRows().values().stream().sorted()
        .map(r -> ImmutableDecisionRowNode.builder()
            .id(r.getId())
            .order(r.getOrder())
            .cells(this.idGen.getHeaders().values().stream().sorted()
                .map(h -> {
                  MutableCell c = h.getRowCell(r.getId());
                  return ImmutableDecisionCellNode.builder().id(c.getId()).value(c.getValue()).header(h.getId()).build();
                })
                .collect(Collectors.toList()))
            .build()
            )
        .collect(Collectors.toList());

    final HitPolicy hitPolicy = this.hitPolicy == null ? HitPolicy.FIRST : this.hitPolicy;
    return ImmutableDecisionTable_AST.builder()
        .name(name)
        .bodyType(Model.BodyType.DECISION_TABLE)
        .description(description)
        .hitPolicy(hitPolicy)
        .headerTypes(headerTypes)
        .headerExpressions(headerExpressions)
        .headers(ImmutableHeaders_AST.builder()
            .acceptDefs(headers.stream().filter(p -> p.getDirection() == Direction.IN).collect(Collectors.toList()))
            .returnDefs(headers.stream().filter(p -> p.getDirection() == Direction.OUT).collect(Collectors.toList()))
            .build())
        .rows(rows);
  }
}
