package io.resys.limaone.spi.ast.flow;



import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.resys.limaone.ast.Flow_AST.AnyFlowNode;
import lombok.Value;



@JsonIgnoreProperties({"parent"})
public class NodeBean implements AnyFlowNode {
  private static final long serialVersionUID = 5409590378906097144L;
  private final String keyword;
  private final String value;
  private final int indent;
  private final NodeSource source;
  private final NodeBean parent;
  private final Map<String, NodeBean> children = new HashMap<>();
  private Integer end;

  public NodeBean(NodeSource source, int indent, String keyword, String value, NodeBean parent) {
    super();
    this.source = source;
    this.keyword = keyword;
    this.value = value;
    this.parent = parent;
    this.indent = indent;
  }

  @Override
  public String getKeyword() {
    return keyword;
  }
  @Override
  public String getValue() {
    return value;
  }
  @Override
  public NodeBean getParent() {
    return parent;
  }
  @Override
  public Map<String, AnyFlowNode> getChildren() {
    return Collections.unmodifiableMap(children);
  }
  public int getIndent() {
    return indent;
  }
  @Override
  public NodeBean get(String keyword) {
    return children.get(keyword);
  }
  public boolean contains(String keyword) {
    return children.get(keyword) != null;
  }
  public NodeBean addChild(NodeSource source, int indent, String keyword, String value) {
    NodeBean result = new NodeBean(source, indent, keyword, value, this);
    children.put(keyword, result);
    return result;
  }
  public NodeBean addChild(NodeBean result) {
    children.put(result.getKeyword(), result);
    return result;
  }
  @Override
  public int getEnd() {
    if(end == null) {
      end = getStart();
      for(NodeBean node : children.values()) {
        if(end < node.getEnd()) {
          end = node.getEnd();
        }
      }
    }
    return end;
  }
  public NodeBean setEnd(int end) {
    this.end = end;
    return this;
  }

  @Override
  public boolean hasNonNull(String name) {
    return this.get(name) != null;
  }

  @Override
  public int getStart() {
    return source == null ? 0 : source.getLineNumber();
  }

  @Override
  public int compareTo(AnyFlowNode o) {
    return Integer.compare(this.getStart(), o.getStart());
  }

  public NodeSource getSource() {
    return source;
  }
  @Override
  public String getSyntax() {
    return source.getLine();
  }
  
  

  @Value
  public static class NodeSource {
    String line;
    int lineNumber;
  }
}
