package io.resys.limaone.ast;
@JsonSerialize(as = TopicBlobBean.class)
@JsonDeserialize(as = TopicBlobBean.class)
interface TopicBlob {
  String getId();
  String getValue();
}
