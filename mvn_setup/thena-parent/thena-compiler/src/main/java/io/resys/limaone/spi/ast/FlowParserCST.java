package io.resys.limaone.spi.ast;

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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.limaone.ast.Flow_CST.YamlInputType;
import io.resys.limaone.ast.ImmutableYamlInputType;
import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.flow.MutableYaml;
import io.resys.limaone.spi.ast.flow.MutableYaml.NodeSource;
import io.resys.limaone.spi.ast.flow.MutableYamlParseTree;
import io.smallrye.mutiny.tuples.Tuple2;



public class FlowParserCST {
  private final static String LINE_SEPARATOR = System.lineSeparator();
  
  
  private final static Collection<YamlInputType> ALL_INPUTS = Collections.unmodifiableList(    
      Arrays.asList(ValueType.STRING, ValueType.BOOLEAN, ValueType.INTEGER, ValueType.LONG, ValueType.DECIMAL, ValueType.DATE, ValueType.DATE_TIME).stream()
      .map(v -> ImmutableYamlInputType.builder().name(v.name()).value(v.name()).build())
      .collect(Collectors.toList())
  );

  private final MutableYamlParseTree result = new MutableYamlParseTree(ALL_INPUTS);
  private final ObjectMapper yamlMapper;
  private final List<ModelError> messages = new ArrayList<>();
  
  public FlowParserCST(AST_ParserProps props) {
    super();
    this.yamlMapper = props.getYaml();
  }
  
  public Tuple2<MutableYamlParseTree, List<ModelError>> parseCST(String joined) {
    final String[] src = joined.split("\\r?\\n");
    final var parseTree = visitFlow(src);
    return Tuple2.of(parseTree, messages);
  }
  public MutableYamlParseTree visitFlow(String[] sourcesAdded) {

    final var iterator = Arrays.asList(sourcesAdded).iterator();
    final var value = new StringBuilder();
    MutableYaml parent = result;

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

      final boolean containsOnlySpaces = src.length() > 0 && "".equals(src.trim());
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
        messages.add(ImmutableModelError.builder()
            .line(lineNumber)
            .msg(message)
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
        messages.add(ImmutableModelError.builder()
            .line(lineNumber)
            .msg(message)
            .build());
        return result.setEnd(lineNumber).setValue(buildSource(value));
      }

      try {
        parent = parent.addChild(new NodeSource(src, lineNumber), indent, keywordAndValue.getKey(), keywordAndValue.getValue());
      } catch(Exception e) {
        messages.add(
            ImmutableModelError.builder()
            .line(lineNumber)
            .msg(e.getMessage())
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
        final var message = String.format("Unknown content on line: %d", lineNumber);
        messages.add(ImmutableModelError.builder().line(lineNumber).msg(message).build());
        return null;
      }
    } catch(IOException e) {
      final var message = String.format("Unknown content on line: %d", lineNumber);
      messages.add(ImmutableModelError.builder().line(lineNumber).msg(message).build());
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
