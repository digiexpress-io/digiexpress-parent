package io.resys.limaone.spi.ast.flow;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.resys.limaone.ast.Flow_AST.AnyFlowNode;

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
}
