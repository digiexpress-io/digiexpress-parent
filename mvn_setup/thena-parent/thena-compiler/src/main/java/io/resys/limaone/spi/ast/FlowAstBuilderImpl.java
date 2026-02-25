package io.resys.limaone.spi.ast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.hash.Hashing;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.AST_Parser.DependencyResolution;
import io.resys.limaone.ast.AST_Parser.FlowParser;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Flow_AST.AnyFlowNode;
import io.resys.limaone.ast.Flow_AST.FlowInputType;
import io.resys.limaone.ast.ImmutableFlowInputType;
import io.resys.limaone.ast.ImmutableFlow_AST;
import io.resys.limaone.ast.ImmutableMessage_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.ast.Simple_AST.Message_AST;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.LocalCache;
import io.resys.limaone.spi.LocalCache.Flow_AST_CacheKey;
import io.resys.limaone.spi.ast.flow.AstFlowNodesFactory;
import io.resys.limaone.spi.ast.flow.NodeBean;
import io.resys.limaone.spi.ast.flow.NodeFlowBean;
import io.resys.limaone.spi.ast.flow.NodeSource;



public class FlowAstBuilderImpl implements AST_Parser.FlowParser {
  private final static String LINE_SEPARATOR = System.lineSeparator();
  
  
  private final static Collection<FlowInputType> ALL_INPUTS = Collections.unmodifiableList(    
      Arrays.asList(ValueType.STRING,  ValueType.BOOLEAN, ValueType.INTEGER, ValueType.LONG, ValueType.DECIMAL, ValueType.DATE, ValueType.DATE_TIME).stream()
      .map(v -> ImmutableFlowInputType.builder().name(v.name()).value(v.name()).build())
      .collect(Collectors.toList())
  );

  private final NodeFlowBean result = new NodeFlowBean(ALL_INPUTS);
  private final ObjectMapper yamlMapper;
  private final List<Message_AST> messages = new ArrayList<>();
  private final List<String> src = new ArrayList<>();
  private String id;
  
  
  public FlowAstBuilderImpl(ObjectMapper yamlMapper) {
    super();
    this.yamlMapper = yamlMapper;
  }
  
  @Override
  public FlowAstBuilderImpl syntax(String src) {
    if (src == null) {
      return this;
    }
    this.src.add(src);
    return this;
  }
  @Override
  public FlowParser deps(DependencyResolution deps) {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public FlowParser id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public Flow_AST parse() {
    Objects.requireNonNull(id, () -> "id must be defined!");
    
    final var joined = String.join(LINE_SEPARATOR, this.src);
    final var hash = Hashing.murmur3_128().hashString(joined, StandardCharsets.UTF_8).toString();
    final var cacheKey = new Flow_AST_CacheKey(hash);
    final Function<Flow_AST_CacheKey, Flow_AST> mappingFunction = (k) -> {
      
      final String[] src = joined.split("\\r?\\n");
      final var flow = visitFlow(src);
      final var ast = ImmutableFlow_AST.builder();
      

      final AnyFlowNode id = flow.getId();
      
      return ast
          .id(this.id)
          .bodyType(Model.BodyType.FLOW)
          .messages(messages)
          .name(id == null ? "": id.getValue())
          .root(flow)
          .headers(AstFlowNodesFactory.headers().build(flow))
          .build();
    };
    return LocalCache.computeIfAbsent(cacheKey, mappingFunction);

  }
  public NodeFlowBean visitFlow(String[] sourcesAdded) {

    final var iterator = Arrays.asList(sourcesAdded).iterator();
    final var value = new StringBuilder();
    NodeBean parent = result;

    int previousLineNumber = 0;
    int lineNumber = 0;
    while(iterator.hasNext()) {
      final var src = iterator.next();
      lineNumber++;

      // add to src
      for(int index = previousLineNumber; index < lineNumber -1; index++) {
        value.append(LINE_SEPARATOR);
      }

      if(src != null) {
        value.append(src);
      }
      value.append(LINE_SEPARATOR);
      previousLineNumber = lineNumber;

      if(src == null) {
        continue;
      }

      boolean containsOnlySpaces = src.length() > 0 && "".equals(src.trim());
      boolean endsWithSpace = src.endsWith(" ");
      if(containsOnlySpaces || endsWithSpace) {
        int start = containsOnlySpaces ? 0 : getSpaceStart(src);
        int end = src.length();
        messages.add(ImmutableMessage_AST.builder()
            .line(lineNumber)
            .range(AstFlowNodesFactory.range().build(start, end))
            .value("space has no meaning")
            .type(Simple_AST.MessageType.WARNING)
            .build());
      }

      if(containsOnlySpaces || src.length() == 0) {
        continue;
      }

      Map.Entry<String, String> keywordAndValue = getKeywordAndValue(src, lineNumber);
      if(keywordAndValue == null) {
        continue;
      }

      int indent = getIndent(src);
      if(indent % 2 != 0) {
        String message = String.format("Incorrect indent: %s, at line: %s!", indent, lineNumber);
        messages.add(ImmutableMessage_AST.builder()
            .line(lineNumber)
            .value(message)
            .type(Simple_AST.MessageType.ERROR)
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
        messages.add(ImmutableMessage_AST.builder()
            .line(lineNumber)
            .value(message)
            .type(Simple_AST.MessageType.ERROR)
            .build());
        return result.setEnd(lineNumber).setValue(buildSource(value));
      }

      try {
        parent = parent.addChild(new NodeSource(src, lineNumber), indent, keywordAndValue.getKey(), keywordAndValue.getValue());
      } catch(Exception e) {
        messages.add(
            ImmutableMessage_AST.builder()
            .line(lineNumber)
            .value(e.getMessage())
            .type(Simple_AST.MessageType.ERROR)
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
        messages.add(ImmutableMessage_AST.builder()
            .line(lineNumber)
            .value(message)
            .type(Simple_AST.MessageType.ERROR)
            .build());
        return null;
      }
    } catch(IOException e) {
      String message = String.format("Unknown content on line: %d", lineNumber);
      messages.add(ImmutableMessage_AST.builder()
          .line(lineNumber)
          .value(message)
          .type(Simple_AST.MessageType.ERROR)
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
