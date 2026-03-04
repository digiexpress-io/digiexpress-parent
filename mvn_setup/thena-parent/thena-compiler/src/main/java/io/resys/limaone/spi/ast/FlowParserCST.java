package io.resys.limaone.spi.ast;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.limaone.model.ImmutableModelError;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.spi.ast.AST_ParserImpl.AST_ParserProps;
import io.resys.limaone.spi.ast.flow.MutableYaml;
import io.resys.limaone.spi.ast.flow.MutableYaml.NodeSource;
import io.resys.limaone.spi.ast.flow.MutableYamlFlow;
import io.smallrye.mutiny.tuples.Tuple2;



public class FlowParserCST {
  private final static String LINE_SEPARATOR = System.lineSeparator();
  
  private final MutableYamlFlow result = new MutableYamlFlow();
  private final ObjectMapper yamlMapper;
  private final List<ModelError> messages = new ArrayList<>();
  
  public FlowParserCST(AST_ParserProps props) {
    super();
    this.yamlMapper = props.getYaml();
  }
  
  public Tuple2<MutableYamlFlow, List<ModelError>> parseCST(String joined) {
    final String[] src = joined.split("\\r?\\n");
    final var parseTree = visitFlow(src);
    return Tuple2.of(parseTree, messages);
  }
  public MutableYamlFlow visitFlow(String[] sourcesAdded) {

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

      final var keywordAndValue = getKeywordAndValue(src, lineNumber);
      if(keywordAndValue == null) {
        continue;
      }

      final var indent = getIndent(src);
      if(indent % 2 != 0) {
        final var message = String.format("Incorrect indent: %s, at line: %s!", indent, lineNumber);
        messages.add(ImmutableModelError.builder()
            .line(lineNumber)
            .msg(message)
            .build());
        continue;
      }

      final var indentToFind = indent - 2;
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
      final Iterator<String> iterator = node.fieldNames();
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
}
