package io.resys.limaone.ast.article;
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
