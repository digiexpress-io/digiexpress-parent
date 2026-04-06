package io.resys.limaone.yaml;

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



import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.resys.limaone.ast.Yaml_CST.Yaml;
import lombok.Getter;
import lombok.Value;



@JsonIgnoreProperties({"parent"})
public class MutableYaml implements Yaml {
  private static final long serialVersionUID = -2089273947467651756L;
  private final String keyword;
  private final int indent;
  private final NodeSource source;
  private final MutableYaml parent;
  private final Map<String, MutableYaml> children = new HashMap<>();
  private Integer end;
  protected String value;

  public MutableYaml(NodeSource source, int indent, String keyword, String value, MutableYaml parent) {
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
  public MutableYaml getParent() {
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
  public MutableYaml get(String keyword) {
    return children.get(keyword);
  }
  public boolean contains(String keyword) {
    return children.get(keyword) != null;
  }
  public MutableYaml addChild(NodeSource source, int indent, String keyword, String value) {
    MutableYaml result = new MutableYaml(source, indent, keyword, value, this);
    children.put(keyword, result);
    return result;
  }
  public MutableYaml addChild(MutableYaml result) {
    children.put(result.getKeyword(), result);
    return result;
  }
  @Override
  public int getEnd() {
    if(end == null) {
      end = getStart();
      for(MutableYaml node : children.values()) {
        if(end < node.getEnd()) {
          end = node.getEnd();
        }
      }
    }
    return end;
  }
  public MutableYaml setEnd(int end) {
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
    return source == null ? null : source.getLine();
  }

  
  public MutableYaml setValue(String value) {
    this.value = value;
    return this;
  }
  
  public boolean isMultilineValue() {
    return false;
  }
  
  public MutableYaml addMultiline(String multiline) {
    return this;
  }
  

  @Getter @Value
  public static class NodeSource {
    String line;
    int lineNumber;
  }
}
