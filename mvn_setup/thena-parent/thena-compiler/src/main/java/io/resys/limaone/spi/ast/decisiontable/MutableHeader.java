package io.resys.limaone.spi.ast.decisiontable;

import java.util.ArrayList;
import java.util.List;

import io.resys.limaone.ast.attribute.Attribute_AST.Direction;
import io.resys.limaone.ast.attribute.Attribute_AST.ValueType;

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