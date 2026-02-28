package io.resys.limaone.spi.compiler.article;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.google.common.hash.Hashing;

import io.resys.limaone.ast.Article_AST;
import io.resys.limaone.ast.Article_AST.ImageTag;
import io.resys.limaone.program.ArticleProgram.TopicBlob;
import io.resys.limaone.program.ArticleProgram.TopicHeading;
import io.resys.limaone.program.ImmutableTopicBlob;
import io.resys.limaone.program.ImmutableTopicHeading;
import jakarta.annotation.Nullable;

public interface Deltas {

  
  @Value.Immutable
  interface TopicData {
    String getPath();
    String getLocale();
    String getValue();
    Boolean getAuth();
    List<Article_AST.Heading> getHeadings();
    List<ImageTag> getImages();
    
    default String getFullPath() {
      return getPath() + "/" + getValue();
    }
    
    default List<TopicHeading> getTopicHeadings() {
      List<TopicHeading> result = new ArrayList<>();
      int index = 1;
      for(final var heading : this.getHeadings()) {      
        result.add(ImmutableTopicHeading.builder()
          .id(String.valueOf(index++))
          .name(heading.getName())
          .order(heading.getOrder())
          .level(heading.getLevel())
          .build());
      }
      return result;
    }
    default TopicBlob getTopicBlob() {
      final String blob = this.getValue();
      final var id = Hashing.murmur3_128().hashString(blob, StandardCharsets.UTF_8).toString();
      return ImmutableTopicBlob.builder().id(id).value(blob).build();      
    }

    default String getParentTopic() {
      final String[] path = this.getPath().split("\\/");
      if(path.length > 1) {
        return path[0];
      }
      return null;
    }
    default String getTopicName() {
      for(final var heading : this.getHeadings()) {
        if(heading.getLevel() == 1 && heading.getName().length() > 1) {
          return heading.getName().substring(1).trim();
        }
      }
      return this.getPath();
    } 
  }
  
  @Value.Immutable
  interface LinkData {
    String getId();
    String getPath();
    String getName();
    String getType();
    String getValue();
    String getLocale();
    Boolean getGlobal();
    Boolean getAnon();
    Boolean getAssignable();
    Boolean getWorkflow();
    @Nullable OffsetDateTime getStartDate();
    @Nullable OffsetDateTime getEndDate();
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
    
    default String hash() {
      final var link = this;
      final var hashString = new StringBuilder()
        .append(link.getPath() != null ? link.getPath() : "")
        .append("|")
        .append(link.getType() != null ? link.getType() : "")
        .append("|")
        .append(link.getName() != null ? link.getName() : "")
        .append("|")
        .append(link.getValue() != null ? link.getValue() : "")
        .append("|")
        .append(link.getGlobal() != null ? link.getGlobal() : false)
        .append("|")
        .append(link.getAssignable() != null ? link.getAssignable() : false)
        .append("|")
        .append(link.getAnon() != null ? link.getAnon() : false)
        .append("|")
        .append(link.getWorkflow() != null ? link.getWorkflow() : false)
        .append("|")
        .append(link.getFormId() != null ? link.getFormId() : "")
        .append("|")
        .append(link.getFormName() != null ? link.getFormName() : "")
        .append("|")
        .append(link.getFormTag() != null ? link.getFormTag() : "")
        .append("|")
        .append(link.getFlowName() != null ? link.getFlowName() : "");
      return Hashing.murmur3_128().hashString(hashString.toString(), StandardCharsets.UTF_8).toString();
    }
  }
  
  @Value.Immutable
  interface ImageData {
    String getPath();
    byte[] getValue();
  }
  
  
  public static <K> Map<String, K> sort(Map<String, K> input) {

    final var values = new ArrayList<>(input.entrySet());
    values.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));

    Map<String, K> result = new LinkedHashMap<>();
    for(final var entry : values) {
      result.put(entry.getKey(), entry.getValue());
    }
    
    return result;
  }
}