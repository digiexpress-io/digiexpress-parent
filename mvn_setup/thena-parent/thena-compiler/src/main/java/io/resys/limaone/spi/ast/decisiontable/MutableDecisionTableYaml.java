package io.resys.limaone.spi.ast.decisiontable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.resys.limaone.ast.DecisionTable_CST.Yaml;
import lombok.Value;

@JsonIgnoreProperties({"parent"})
public class MutableDecisionTableYaml implements Yaml {
  private static final long serialVersionUID = 5409590378906097144L;
  private final String keyword;
  private final String value;
  private final int indent;
  private final NodeSource source;
  private final MutableDecisionTableYaml parent;
  private final Map<String, MutableDecisionTableYaml> children = new HashMap<>();
  private Integer end;

  public MutableDecisionTableYaml(NodeSource source, int indent, String keyword, String value, MutableDecisionTableYaml parent) {
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
  public MutableDecisionTableYaml getParent() {
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
  public MutableDecisionTableYaml get(String keyword) {
    return children.get(keyword);
  }
  public boolean contains(String keyword) {
    return children.get(keyword) != null;
  }
  public MutableDecisionTableYaml addChild(NodeSource source, int indent, String keyword, String value) {
    MutableDecisionTableYaml result = new MutableDecisionTableYaml(source, indent, keyword, value, this);
    children.put(keyword, result);
    return result;
  }
  public MutableDecisionTableYaml addChild(MutableDecisionTableYaml result) {
    children.put(result.getKeyword(), result);
    return result;
  }
  @Override
  public int getEnd() {
    if(end == null) {
      end = getStart();
      for(MutableDecisionTableYaml node : children.values()) {
        if(end < node.getEnd()) {
          end = node.getEnd();
        }
      }
    }
    return end;
  }
  public MutableDecisionTableYaml setEnd(int end) {
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
    return source.getLine();
  }

  @Value
  public static class NodeSource {
    String line;
    int lineNumber;
  }
}