package io.resys.limaone.spi.ast.decisiontable;

public class MutableRow implements Comparable<MutableRow> {
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