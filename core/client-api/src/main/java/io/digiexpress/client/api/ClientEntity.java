package io.digiexpress.client.api;

import java.beans.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface ClientEntity {
 enum ClientEntityType { PROJECT, SERVICE_DEF, SERVICE_RELEASE }
 enum ConfigType { STENCIL, DIALOB, HDES, PROJECT, RELEASE }
 
 @Nullable String getId(); // unique id
 @Nullable String getVersion(); // not really nullable, just in serialization
 LocalDateTime getCreated();
 LocalDateTime getUpdated();
 ClientEntityType getType();
 
 @Value.Immutable @JsonSerialize(as = ImmutableProject.class) @JsonDeserialize(as = ImmutableProject.class)
 interface Project extends ClientEntity {
   String getName();
   String getHead(); //latest project id
   ProjectConfig getConfig();
   List<ProjectRevision> getRevisions();
   
   @Value.Default default ClientEntityType getType() { return ClientEntityType.PROJECT; }
   @JsonIgnore default String getHeadDefId() { return getRevisions().stream().filter(e -> e.getId().equals(getHead())).map(e -> e.getDefId()).findFirst().get(); }
 }
 
 @Value.Immutable @JsonSerialize(as = ImmutableServiceDefinition.class) @JsonDeserialize(as = ImmutableServiceDefinition.class)
 interface ServiceDefinition extends ClientEntity {
   List<RefIdValue> getRefs(); // stencil and wrench
   String getProjectId();
   List<ServiceDescriptor> getDescriptors();
   
   @Value.Default default ClientEntityType getType() { return ClientEntityType.SERVICE_DEF; }
   @JsonIgnore @Transient @Nullable 
   Project getProject();
   
   @JsonIgnore
   default RefIdValue getHdes() { return this.getRefs().stream().filter(e -> e.getType().equals(ConfigType.HDES)).findFirst().get(); }
   @JsonIgnore
   default RefIdValue getStencil() { return this.getRefs().stream().filter(e -> e.getType().equals(ConfigType.STENCIL)).findFirst().get(); }
 }

 @Value.Immutable @JsonSerialize(as = ImmutableServiceRelease.class) @JsonDeserialize(as = ImmutableServiceRelease.class)
 interface ServiceRelease extends ClientEntity {
   LocalDateTime getActiveFrom();
   String getRepoId();
   String getName();
   List<String> getDesc();
   List<ServiceReleaseValue> getValues();
   @Value.Default
   default ClientEntityType getType() { return ClientEntityType.SERVICE_RELEASE; }
 }
 


 @Value.Immutable @JsonSerialize(as = ImmutableServiceDescriptor.class) @JsonDeserialize(as = ImmutableServiceDescriptor.class)
 interface ServiceDescriptor extends Serializable {
   @Nullable String getId();
   String getName();
   String getDesc();
   String getFlowId();
   String getFormId();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableProjectConfig.class) @JsonDeserialize(as = ImmutableProjectConfig.class)
 interface ProjectConfig extends Serializable {
   String getStencil();
   String getDialob();
   String getHdes();
   String getProject();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableRefIdValue.class) @JsonDeserialize(as = ImmutableRefIdValue.class)
 interface RefIdValue extends Serializable {
   @Nullable String getId();
   String getTagName();
   String getRepoId();
   ConfigType getType(); 
 }
 @Value.Immutable @JsonSerialize(as = ImmutableProjectRevision.class) @JsonDeserialize(as = ImmutableProjectRevision.class)
 interface ProjectRevision extends Serializable {
   String getId();
   String getRevisionName(); // iteration X
   String getDefId();
   LocalDateTime getCreated();
   LocalDateTime getUpdated();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableServiceReleaseValue.class) @JsonDeserialize(as = ImmutableServiceReleaseValue.class)
 interface ServiceReleaseValue extends Serializable {
   String getId(); // hdes or stencil tagname/dialob form id
   String getBody();
   String getBodyHash();
   ConfigType getBodyType();
 }
}
