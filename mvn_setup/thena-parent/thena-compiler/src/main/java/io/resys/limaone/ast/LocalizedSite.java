package io.resys.limaone.ast;

import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = ImmutableLocalizedSite.class)
@JsonDeserialize(as = ImmutableLocalizedSite.class)
@Value.Immutable
interface LocalizedSite {
  String getId();
  String getImages();
  String getLocale();
  
  Map<String, Topic> getTopics();
  Map<String, TopicBlob> getBlobs();
  Map<String, TopicLink> getLinks();
  Map<String, List<TopicLink>> getWorkflowsInOtherLocales();
}
