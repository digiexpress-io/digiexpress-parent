package io.thestencil.client.tests.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.git.TreeValue;
import io.resys.thena.spi.DbState;
import io.thestencil.client.api.StencilClient.EntityType;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TestExporter {
  private final DbState state;

  public TestExporter(DbState state) {
    super();
    this.state = state;
  }

  public String print(Tenant repo) {
    final Map<String, String> replacements = new HashMap<>();
    final Function<String, String> ID = (id) -> {
      if(replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

    final var ctx = state.toGitState(repo);
    
    StringBuilder result = new StringBuilder();

    result
    .append(System.lineSeparator())
    .append("Repo").append(System.lineSeparator())
    .append("  - id: ").append(ID.apply(repo.getId()))
    .append(", rev: ").append(ID.apply(repo.getRev())).append(System.lineSeparator())
    .append("    name: ").append(repo.getName())
    .append(System.lineSeparator());
    
    result
    .append(System.lineSeparator())
    .append("Refs").append(System.lineSeparator());
    
    ctx.query().refs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ")
      .append(ID.apply(item.getCommit())).append(": ").append(item.getName())
      .append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();

    
    result
    .append(System.lineSeparator())
    .append("Tags").append(System.lineSeparator());
    
    ctx.query().tags()
    .find().onItem()
    .transform(item -> {
      result.append("  - id: ").append(item.getName())
      .append(System.lineSeparator())
      .append("    commit: ").append(ID.apply(item.getCommit()))
      .append(", message: ").append(item.getMessage())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    result
    .append(System.lineSeparator())
    .append("Commits").append(System.lineSeparator());
    
    ctx.query().commits()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - id: ").append(ID.apply(item.getId()))
      .append(System.lineSeparator())
      .append("    tree: ").append(ID.apply(item.getTree()))
      .append(", parent: ").append(item.getParent().map(e -> ID.apply(e)).orElse(""))
      .append(", message: ").append(item.getMessage())
      .append(System.lineSeparator());
      
      return item;
    }).collect().asList().await().indefinitely();
    
    
    result
    .append(System.lineSeparator())
    .append("Trees").append(System.lineSeparator());
    
    ctx.query().trees()
    .findAll().onItem()
    .transform(src -> {
      
      final var items = new ArrayList<TreeValue>(src.getValues().values());
      items.sort(new Comparator<TreeValue>() {

        @Override
        public int compare(TreeValue o1, TreeValue o2) {
          return o1.getName().compareTo(o2.getName());
        }
        
      });
      
      result.append("  - id: ").append(ID.apply(src.getId())).append(System.lineSeparator());
      for(final var e : items) {
          result.append("    ")
            .append(ID.apply(e.getBlob()))
            .append(": ")
            .append(e.getName())
            .append(System.lineSeparator());
        
      }
      
      return src;
    }).collect().asList().await().indefinitely();
    
    
    
    result
    .append(System.lineSeparator())
    .append("Blobs").append(System.lineSeparator());
    
    ctx.query().blobs()
    .findAll().onItem()
    .transform(item -> {
      result.append("  - ").append(ID.apply(item.getId())).append(": ").append(replaceContent(item.getValue().toString(), replacements)).append(System.lineSeparator());
      return item;
    }).collect().asList().await().indefinitely();
    
    return result.toString();
  }
  
  public static String replaceContent(String text, Map<String, String> replacements) {
    try {
      JsonNode entity = TestUtils.objectMapper.readTree(text);
      
      if(entity.get("type").textValue().equals(EntityType.RELEASE.toString())) {
        ((ObjectNode) entity.get("body")).set("created", TextNode.valueOf(""));
        text = entity.toString();
      }
      
      String newText = text;
      for(Map.Entry<String, String> entry : replacements.entrySet()) {
        newText = newText.replaceAll(entry.getKey(), entry.getValue());
      }
      
      return newText;
    } catch(IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  

  public static String toString(Class<?> type, String resource) {
    try {
      return IOUtils.toString(type.getClassLoader().getResource(resource), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
