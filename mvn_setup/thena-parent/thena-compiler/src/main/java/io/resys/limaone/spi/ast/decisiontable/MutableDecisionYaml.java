package io.resys.limaone.spi.ast.decisiontable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.resys.limaone.ast.DecisionTable_CST.Yaml;
import lombok.Value;

@JsonIgnoreProperties({"parent"})
public class MutableDecisionYaml implements Yaml {
  private static final long serialVersionUID = 5409590378906097144L;
  private final String keyword;
  private final String value;
  private final int indent;
  private final NodeSource source;
  private final MutableDecisionYaml parent;
  private final Map<String, MutableDecisionYaml> children = new HashMap<>();
  private Integer end;

  public MutableDecisionYaml(NodeSource source, int indent, String keyword, String value, MutableDecisionYaml parent) {
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
  public MutableDecisionYaml getParent() {
    return parent;
  }
  @Override
  public Map<String, Yaml> getChildren() {
    return Collections.unmodifiableMap(children);
  }
  public int getIndent() {
    return indent;
  }
  @Override
  public MutableDecisionYaml get(String keyword) {
    return children.get(keyword);
  }
  public boolean contains(String keyword) {
    return children.get(keyword) != null;
  }
  public MutableDecisionYaml addChild(NodeSource source, int indent, String keyword, String value) {
    MutableDecisionYaml result = new MutableDecisionYaml(source, indent, keyword, value, this);
    children.put(keyword, result);
    return result;
  }
  public MutableDecisionYaml addChild(MutableDecisionYaml result) {
    children.put(result.getKeyword(), result);
    return result;
  }
  @Override
  public int getEnd() {
    if(end == null) {
      end = getStart();
      for(MutableDecisionYaml node : children.values()) {
        if(end < node.getEnd()) {
          end = node.getEnd();
        }
      }
    }
    return end;
  }
  public MutableDecisionYaml setEnd(int end) {
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
  public int compareTo(Yaml o) {
    return Integer.compare(this.getStart(), o.getStart());
  }

  public NodeSource getSource() {
    return source;
  }
  @Override
  public String getSyntax() {
    return getSource().getLine();
  }
  @Value
  public static class NodeSource {
    String line;
    int lineNumber;
  }
}