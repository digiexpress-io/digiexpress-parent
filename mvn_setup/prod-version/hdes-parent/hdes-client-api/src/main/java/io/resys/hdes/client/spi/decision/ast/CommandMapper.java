package io.resys.hdes.client.spi.decision.ast;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.*;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionRow;
import io.resys.hdes.client.api.ast.AstDecision.ColumnExpressionType;
import io.resys.hdes.client.api.ast.AstDecision.HitPolicy;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.DecisionAstException;
import io.resys.hdes.client.api.programs.ExpressionProgram;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class CommandMapper {
  private final static List<String> headerTypes = Collections.unmodifiableList(
      Arrays.asList(ValueType.STRING,  ValueType.BOOLEAN, ValueType.INTEGER, ValueType.LONG, ValueType.DECIMAL, ValueType.DATE, ValueType.DATE_TIME).stream()
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
  
  public static Builder builder(HdesTypesMapper dataTypeFactory) {
    return new Builder(dataTypeFactory);
  }

  public static class Builder {
    private final HdesTypesMapper typeDefs; 
    private String name;
    private String description;
    private HitPolicy hitPolicy;
    private final IdFixer idGen = new IdFixer();

    public Builder(HdesTypesMapper dataTypeFactory) {
      super();
      this.typeDefs = dataTypeFactory;
    }
    private ValueType getValueType(MutableHeader header) {
      return header.getValue();
    }
    public Builder name(String name) {
      this.name = name;
      return this;
    }
    public Builder description(String description) {
      this.description = description;
      return this;
    }
    public Builder hitPolicy(HitPolicy hitPolicy) {
      this.hitPolicy = hitPolicy;
      return this;
    }
    public Map.Entry<String, Builder> addHeader(Direction direction, String name) {
      MutableHeader header = idGen.addHeader()
          .setDirection(direction)
          .setName(name)
          .setValue(ValueType.STRING);
      return new AbstractMap.SimpleImmutableEntry<String, Builder>(header.getId(), this);
    }
    public Builder changeHeaderType(String id, String value) {
      try {
        idGen.getHeader(id).setValue(ValueType.valueOf(value));
      } catch(Exception e) {
        idGen.getHeader(id).setValue(null);
      }
      
      return this;
    }
    public Builder changeHeaderScript(String id, String value) {
      idGen.getHeader(id).setScript(value);
      return this;
    }
    public Builder changeHeaderName(String id, String value) {
      idGen.getHeader(id).setName(value);
      return this;
    }
    public Builder changeHeaderExtRef(String id, String value) {
      idGen.getHeader(id).setExtRef(value);
      return this;
    }
    public Builder changeHeaderDirection(String id, Direction value) {
      MutableHeader header = idGen.getHeader(id).setDirection(value);
      ValueType valueType = getValueType(header);

      // Remove expression if cell new direction is out
      if(value == Direction.OUT && valueType != null) {
        header.getCells().stream()
        .filter(c -> !StringUtils.isEmpty(c.getValue()))
        .forEach(cell -> {

          try {
            ExpressionProgram expression = typeDefs.expression(valueType, cell.getValue());
            if(expression.getConstants().size() == 1) {
              cell.setValue(expression.getConstants().get(0));
            }
          } catch(DecisionAstException e) {
            cell.setValue(cell.getValue());
          }
        });
      }

      return this;
    }

    private String getExpression(ValueType valueType, ColumnExpressionType value, String columnValue) {
      String constant;
      try {
        ExpressionProgram expression = typeDefs.expression(valueType, columnValue);
        if(expression.getConstants().size() != 1) {
          return null;
        }
        constant = expression.getConstants().get(0);
      } catch(DecisionAstException e) {
        constant = columnValue.trim();
      }
      switch (value) {
      case EQUALS:
        return "= " + constant;
      case IN:
        return "in[\"" + constant + "\"]";
      default:
        return null;
      }
    }

    public Builder setHeaderExpression(String id, ColumnExpressionType value) {
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
    public Builder changeCell(String id, String value) {
      idGen.getCell(id).setValue(value);
      return this;
    }
    public Builder changeCell(String rowId, int columnIndex, String value) {
      MutableHeader column = idGen.getHeaders().values().stream().filter(r -> r.order == columnIndex).findFirst().get();
      MutableCell cell = column.getCells().stream().filter(c -> c.getRow().equals(rowId)).findFirst().get();
      cell.setValue(value);
      return this;
    }
    public Builder deleteCell(String id) {
      idGen.getCell(id).setValue(null);
      return this;
    }
    public Builder deleteHeader(String id) {
      idGen.deleteHeader(id);
      return this;
    }
    public Map.Entry<String, Builder> addRow() {
      final var row = idGen.addRow();
      return new AbstractMap.SimpleImmutableEntry<String, Builder>(row.getId(), this);
    }
    public Builder deleteRow(String id) {
      idGen.deleteRow(id);
      return this;
    }
    public Builder deleteRows() {
      new ArrayList<>(idGen.getRows().keySet()).forEach(id -> deleteRow(id));
      return this;
    }
    public Builder deleteColumns() {
      new ArrayList<>(idGen.getHeaders().keySet()).forEach(id -> deleteHeader(id));
      return this;
    }
    public Builder moveRow(String srcId, String targetId) {
      MutableRow src = idGen.getRow(srcId);
      MutableRow target = idGen.getRow(targetId);

      int targetOrder = src.getOrder();
      int srcOrder = target.getOrder();
      src.setOrder(srcOrder);
      target.setOrder(targetOrder);
      return this;
    }
    public Builder insertRow(String srcId, String targetId) {
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
    
    public Builder copyRow(String srcId) {
      MutableRow src = idGen.getRow(srcId);
      String targetId = addRow().getKey();
      
      for(MutableHeader header : this.idGen.getHeaders().values()) {
        MutableCell from = header.getCells().stream().filter(c -> c.getRow().equals(src.getId())).findFirst().get();
        MutableCell to = header.getCells().stream().filter(c -> c.getRow().equals(targetId)).findFirst().get();
        to.setValue(from.getValue());
      }
      
      return insertRow(targetId, srcId);
    }

    public Builder moveHeader(String srcId, String targetId) {
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

    public Builder setValueSet(String id, String values) {
      if (values.length() > 0) {
        List<String> valueList = Arrays.asList(values.split(", "));
        idGen.getHeader(id).setValueSet(valueList);
        return this;
      }
      idGen.getHeader(id).setValueSet(new ArrayList<>());
      return this;
    }

    private String resolveScriptValue(MutableHeader header, MutableCell cell) {
      Map<String, Object> context = new HashMap<>();
      for(MutableHeader h : idGen.getHeaders().values()) {
        MutableCell value = h.getCells().stream()
            .filter(c -> c.getRow().equals(cell.getRow()))
            .findFirst().get();
        try {
          Object variable = parseVariable(value.getValue(), h.getValue());
          context.put(h.getName(), variable);
        } catch(Exception e) {
        }
      }
      
      try {
        return typeDefs.expression(ValueType.MAP, header.getScript()).run(context) + "";
      } catch(Exception e) {
        return null;
      }
    }

    public AstDecision build() {
      this.idGen.getHeaders().values().stream()
      .filter(h -> !StringUtils.isEmpty(h.getScript()))
      .forEach(h -> h.getCells().forEach(c -> c.setValue(resolveScriptValue(h, c))));
      
      List<TypeDef> headers = this.idGen.getHeaders().values().stream().sorted()
          .map(h -> (TypeDef) typeDefs.dataType()
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
      
      List<AstDecisionRow> rows = this.idGen.getRows().values().stream().sorted()
          .map(r -> ImmutableAstDecisionRow.builder()
            .id(r.getId())
            .order(r.getOrder())
            .cells(this.idGen.getHeaders().values().stream().sorted()
                .map(h -> {
                  MutableCell c = h.getRowCell(r.getId());
                  return ImmutableAstDecisionCell.builder().id(c.getId()).value(c.getValue()).header(h.getId()).build();
                })
                .collect(Collectors.toList()))
            .build()
          )
          .collect(Collectors.toList());
      
      final HitPolicy hitPolicy = this.hitPolicy == null ? HitPolicy.ALL : this.hitPolicy;
      return ImmutableAstDecision.builder()
          .name(name)
          .bodyType(AstBodyType.DT)
          .description(description)
          .hitPolicy(hitPolicy)
          .headerTypes(headerTypes)
          .headerExpressions(headerExpressions)
          .headers(ImmutableHeaders.builder()
              .acceptDefs(headers.stream().filter(p -> p.getDirection() == Direction.IN).collect(Collectors.toList()))
              .returnDefs(headers.stream().filter(p -> p.getDirection() == Direction.OUT).collect(Collectors.toList()))
              .build())
          .rows(rows)
          .build();
    }
  }

  

  public static class MutableHeader implements Comparable<MutableHeader> {

    private final String id;
    private Direction direction;

    private String script;
    private String name;
    private String extRef;
    private ValueType value;
    private int order;
    private final List<MutableCell> cells = new ArrayList<>();
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

  public static class MutableCell {
    private final String id;
    private final String row;
    private String value;

    public MutableCell(String id, String row) {
      super();
      this.id = id;
      this.row = row;
    }
    public String getValue() {
      return value;
    }
    public void setValue(String value) {
      this.value = value;
    }
    public String getRow() {
      return row;
    }
    public String getId() {
      return id;
    }
  }
  public static class MutableRow implements Comparable<MutableRow> {
    private final String id;
    private int order;
    public MutableRow(String id, int order) {
      super();
      this.id = id;
      this.order = order;
    }
    public int getOrder() {
      return order;
    }
    public void setOrder(int order) {
      this.order = order;
    }
    public String getId() {
      return id;
    }
    @Override
    public int compareTo(MutableRow o) {
      return Integer.compare(order, o.order);
    }
  }
}
