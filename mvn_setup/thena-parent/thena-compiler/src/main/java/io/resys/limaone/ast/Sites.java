package io.resys.limaone.ast;
@Value.Immutable
@JsonSerialize(as = ImmutableSites.class)
@JsonDeserialize(as = ImmutableSites.class)
interface Sites {
  String getTagName();
  Long getCreated();
  Map<String, LocalizedSite> getSites();
}
