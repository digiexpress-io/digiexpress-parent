package io.resys.limaone.ast.article;

@JsonSerialize(as = ImmutableTopicLink.class)
@JsonDeserialize(as = ImmutableTopicLink.class)
@Value.Immutable
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
