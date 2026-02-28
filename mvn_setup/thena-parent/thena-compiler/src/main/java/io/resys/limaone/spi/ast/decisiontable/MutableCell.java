package io.resys.limaone.spi.ast.decisiontable;

public class MutableCell {
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