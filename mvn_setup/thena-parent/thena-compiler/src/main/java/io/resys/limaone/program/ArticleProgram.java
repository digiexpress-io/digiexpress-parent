package io.resys.limaone.program;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface ArticleProgram extends Program {
  
  ArticleProgramResult run(Object context);
  
  @Value.Immutable @JsonSerialize(as = ImmutableArticleProgramResult.class) @JsonDeserialize(as = ImmutableArticleProgramResult.class)
  interface ArticleProgramResult extends ProgramResult {
    String getTagName();
    OffsetDateTime getStartDate();
    OffsetDateTime getEndDate();
    Map<String, LocalizedSite> getSites();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableLocalizedSite.class) @JsonDeserialize(as = ImmutableLocalizedSite.class)
  interface LocalizedSite {
    String getId();
    String getImages();
    String getLocale();
    
    Map<String, Topic> getTopics();
    Map<String, TopicBlob> getBlobs();
    Map<String, TopicLink> getLinks();
    Map<String, List<TopicLink>> getWorkflowsInOtherLocales();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableTopicBlob.class) @JsonDeserialize(as = ImmutableTopicBlob.class)
  interface TopicBlob {
    String getId();
    String getValue();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableTopic.class) @JsonDeserialize(as = ImmutableTopic.class)
  interface Topic {
    String getId();
    String getName();
    
    List<String> getLinks();
    List<TopicHeading> getHeadings();
    @Nullable String getParent();
    @Nullable String getBlob();
    
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = JsonOnlyTrue.class)
    @Nullable Boolean getAuth();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableTopicHeading.class) @JsonDeserialize(as = ImmutableTopicHeading.class)
  interface TopicHeading {
    String getId();
    String getName();
    Integer getOrder();
    Integer getLevel();
  }
  
  @Value.Immutable @JsonSerialize(as = ImmutableTopicLink.class) @JsonDeserialize(as = ImmutableTopicLink.class)
  interface TopicLink {
    String getId();
    @Nullable String getPath();
    String getType();
    String getName();
    String getValue();
    Boolean getGlobal();
    Boolean getWorkflow();
    
    @Nullable Boolean getAssignable();
    @Nullable Boolean getAnon();
    @Nullable LocalDateTime getStartDate();
    @Nullable LocalDateTime getEndDate();
    @Nullable String getFormId();
    @Nullable String getFormName();
    @Nullable String getFormTag();
    @Nullable String getFlowName();
  }
  
  public static class JsonOnlyTrue {
    @Override
    public boolean equals(Object obj) {
      // Include in JSON only if value is true
      return obj == null || !Boolean.TRUE.equals(obj);
    }
  }
}
