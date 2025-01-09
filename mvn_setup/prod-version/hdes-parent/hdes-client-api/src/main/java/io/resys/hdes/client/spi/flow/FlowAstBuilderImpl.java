package io.resys.hdes.client.spi.flow;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.hdes.client.api.HdesAstTypes.FlowAstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBody.AstCommandMessage;
import io.resys.hdes.client.api.ast.AstBody.CommandMessageType;
import io.resys.hdes.client.api.ast.AstChangeset;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowInputType;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.ImmutableAstCommandMessage;
import io.resys.hdes.client.api.ast.ImmutableAstFlow;
import io.resys.hdes.client.api.ast.ImmutableAstFlowInputType;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.FlowAstException;
import io.resys.hdes.client.spi.changeset.AstChangesetFactory;
import io.resys.hdes.client.spi.config.HdesClientConfig.AstFlowNodeVisitor;
import io.resys.hdes.client.spi.flow.ast.AstFlowNodesFactory;
import io.resys.hdes.client.spi.flow.ast.beans.NodeBean;
import io.resys.hdes.client.spi.flow.ast.beans.NodeFlowBean;
import io.resys.hdes.client.spi.util.HdesAssert;

public class FlowAstBuilderImpl implements FlowAstBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(FlowAstBuilderImpl.class);
  private final static String LINE_SEPARATOR = System.lineSeparator();
  private final static Collection<AstFlowInputType> inputTypes = Collections.unmodifiableList(    
      Arrays.asList(ValueType.STRING,  ValueType.BOOLEAN, ValueType.INTEGER, ValueType.LONG, ValueType.DECIMAL, ValueType.DATE, ValueType.DATE_TIME).stream()
      .map(v -> ImmutableAstFlowInputType.builder().name(v.name()).value(v.name()).build())
      .collect(Collectors.toList())
  );

  private final Collection<AstFlowNodeVisitor> visitors = new ArrayList<>();
  private final NodeFlowBean result = new NodeFlowBean(inputTypes);
  private final ObjectMapper yamlMapper;
  private final HdesTypesMapper typeDefs;
  private final List<AstCommandMessage> messages = new ArrayList<>();
  private List<AstCommand> src = new ArrayList<>();
  private Integer rev;
  
  public FlowAstBuilderImpl(ObjectMapper yamlMapper, HdesTypesMapper typeDefs, Collection<AstFlowNodeVisitor> visitors) {
    super();
    this.yamlMapper = yamlMapper;
    this.typeDefs = typeDefs;
    this.visitors.addAll(visitors);
  }
  
  @Override
  public FlowAstBuilderImpl src(ArrayNode src) {
    if(src == null) {
      return this;
    }
    for(JsonNode node : src) {
      final String type = getString(node, "type");
      this.src.add(ImmutableAstCommand.builder().id(getString(node, "id")).value(getString(node, "value")).type(AstCommandValue.valueOf(type)).build());
    }
    return this;
  }
  
  @Override
  public FlowAstBuilder src(List<AstCommand> src) {
    if(src == null) {
      return this;
    }
    this.src.addAll(src);
    return this;
  }

  @Override
  public FlowAstBuilder srcAdd(int line, String value) {
    this.src.add(ImmutableAstCommand.builder().id(line + "").value(value).type(AstCommandValue.ADD).build());
    return this;
  }

  @Override
  public FlowAstBuilder srcDel(int line) {
    this.src.add(ImmutableAstCommand.builder().id(line + "").value(line + "").type(AstCommandValue.DELETE).build());
    return this;
  }
  @Override
  public FlowAstBuilderImpl rev(Integer rev) {
    this.rev = rev;
    return this;
  }

  @Override
  public AstFlow build() {
    HdesAssert.notNull(src, () -> "src can't ne null!");

    final var changes = AstChangesetFactory.src(src, rev);
    final var flow = visitFlow(changes.getSrc());
    final var ast = ImmutableAstFlow.builder();
    
    try {
      visitors.stream().forEach(v -> v.visit(flow, ast));
    } catch(Exception e) {
      LOGGER.error(e.getMessage(), e);
      messages.add(
          ImmutableAstCommandMessage.builder()
          .line(0)
          .value("message: " + e.getMessage())
          .type(CommandMessageType.ERROR)
          .build());
    }
    
    AstFlowNode id = flow.getId();
    
    return ast
        .bodyType(AstBodyType.FLOW)
        .messages(messages)
        .name(id == null ? "": id.getValue())
        .src(flow)
        .headers(AstFlowNodesFactory.headers(typeDefs).build(flow))
        .build();
  }
  public NodeFlowBean visitFlow(List<AstChangeset> sourcesAdded) {

    Iterator<AstChangeset> iterator = sourcesAdded.iterator();
    NodeBean parent = result;
    StringBuilder value = new StringBuilder();

    int previousLineNumber = 0;
    int lineNumber = 0;
    while(iterator.hasNext()) {
      AstChangeset src = iterator.next();
      String lineContent = src.getCommands().get(src.getCommands().size() - 1).getValue();
      lineNumber = src.getLine();

      // add to src
      for(int index = previousLineNumber; index < lineNumber -1; index++) {
        value.append(LINE_SEPARATOR);
      }

      if(lineContent != null) {
        value.append(lineContent);
      }
      value.append(LINE_SEPARATOR);
      previousLineNumber = lineNumber;

      if(lineContent == null) {
        continue;
      }

      boolean containsOnlySpaces = lineContent.length() > 0 && "".equals(lineContent.trim());
      boolean endsWithSpace = lineContent.endsWith(" ");
      if(containsOnlySpaces || endsWithSpace) {
        int start = containsOnlySpaces ? 0 : getSpaceStart(lineContent);
        int end = lineContent.length();
        messages.add(ImmutableAstCommandMessage.builder()
            .line(lineNumber)
            .range(AstFlowNodesFactory.range().build(start, end))
            .value("space has no meaning")
            .type(CommandMessageType.WARNING)
            .build());
      }

      if(containsOnlySpaces || lineContent.length() == 0) {
        continue;
      }

      Map.Entry<String, String> keywordAndValue = getKeywordAndValue(lineContent, lineNumber);
      if(keywordAndValue == null) {
        continue;
      }

      int indent = getIndent(lineContent);
      if(indent % 2 != 0) {
        String message = String.format("Incorrect indent: %s, at line: %s!", indent, lineNumber);
        messages.add(ImmutableAstCommandMessage.builder()
            .line(lineNumber)
            .value(message)
            .type(CommandMessageType.ERROR)
            .build());
        continue;
      }

      int indentToFind = indent - 2;
      while(parent != null) {
        if(parent.getIndent() <= indentToFind) {
          break;
        }
        parent = parent.getParent();
      }

      if(parent == null) {
        String message = String.format("Incorrect indent at line: %s, expecting: %s but was: %s!", lineNumber, indentToFind, indent);
        messages.add(ImmutableAstCommandMessage.builder()
            .line(lineNumber)
            .value(message)
            .type(CommandMessageType.ERROR)
            .build());
        return result.setEnd(lineNumber).setValue(buildSource(value));
      }

      try {
        parent = parent.addChild(src, indent, keywordAndValue.getKey(), keywordAndValue.getValue());
      } catch(FlowAstException e) {
        messages.add(
            ImmutableAstCommandMessage.builder()
            .line(lineNumber)
            .value(e.getMessage())
            .type(CommandMessageType.ERROR)
            .build());
        return result.setEnd(lineNumber).setValue(value.toString());
      }
    }

    return result.setEnd(lineNumber).setValue(buildSource(value));
  }
  
  private String buildSource(StringBuilder value) {
    String result = value.toString();
    if(result.endsWith(LINE_SEPARATOR)) {
      return result.substring(0, result.length() - LINE_SEPARATOR.length());
    }
    return result;
  }

  private static int getSpaceStart(String lineContent) {
    char[] charArray = lineContent.toCharArray();
    int index = charArray.length;
    do {
      index--;
      if(charArray[index] != ' ') {
        break;
      }
    } while(index > -1);
    return index + 1;
  }

  private Map.Entry<String, String> getKeywordAndValue(String lineContent, int lineNumber) {
    String value;
    String keyword;
    try {
      JsonNode node = yamlMapper.readTree(lineContent);
      if(node == null || node.isNull()) {
        return null;
      }
      node = node.isArray() ? ((ArrayNode) node).iterator().next() : node;
      Iterator<String> iterator = node.fieldNames();
      if(iterator.hasNext()) {
        keyword = iterator.next();
        JsonNode nodeValue = node.get(keyword);
        if (node.isNull()) {
          value = null;
        } else if (nodeValue.isArray()) {
          value = nodeValue.toString();
        } else {
          value = nodeValue.asText();
        }
      } else {
        String message = String.format("Unknown content on line: %d", lineNumber);
        messages.add(ImmutableAstCommandMessage.builder()
            .line(lineNumber)
            .value(message)
            .type(CommandMessageType.ERROR)
            .build());
        return null;
      }
    } catch(IOException e) {
      String message = String.format("Unknown content on line: %d", lineNumber);
      messages.add(ImmutableAstCommandMessage.builder()
          .line(lineNumber)
          .value(message)
          .type(CommandMessageType.ERROR)
          .build());
      return null;
    }
    return new AbstractMap.SimpleImmutableEntry<String, String>(keyword, value);
  }

  private static int getIndent(String value){
    char[] characters = value.toCharArray();
    for(int index = 0; index < value.length(); index++){
      if(!Character.isWhitespace(characters[index])){
        return index;
      }
    }
    return 0;
  }
  protected String getString(JsonNode node, String name) {
    return node.hasNonNull(name) ? node.get(name).asText() : null;
  }

}
