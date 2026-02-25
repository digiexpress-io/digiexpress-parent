package io.resys.limaone.ast.article;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.thestencil.client.spi.beans.TopicHeadingBean;

@JsonSerialize(as = ImmutableTopic.class)
@JsonDeserialize(as = ImmutableTopic.class)
@Value.Immutable
interface Topic {
  String getId();
  String getName();
  
  List<String> getLinks();
  List<TopicHeading> getHeadings();
  @Nullable String getParent();
  @Nullable String getBlob();
  
  @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = JsonOnlyTrue.class)
  @Nullable Boolean getAuth();
  
  public static class JsonOnlyTrue {
    @Override
    public boolean equals(Object obj) {
      // Include in JSON only if value is true
      return obj == null || !Boolean.TRUE.equals(obj);
    }
  }
  
  @JsonSerialize(as = TopicHeadingBean.class)
  @JsonDeserialize(as = TopicHeadingBean.class)
  interface TopicHeading {
    String getId();
    String getName();
    Integer getOrder();
    Integer getLevel();
  }
  
}