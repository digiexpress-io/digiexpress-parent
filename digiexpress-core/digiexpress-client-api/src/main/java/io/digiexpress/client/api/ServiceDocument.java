package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface ServiceDocument {
 enum DocumentType { SERVICE_REV, SERVICE_DEF, SERVICE_CONFIG, SERVICE_RELEASE }
 enum ConfigType { STENCIL, DIALOB, HDES, SERVICE }
 
 @Nullable String getId(); // unique id
 @Nullable String getVersion(); // not really nullable, just in serialization
 LocalDateTime getCreated();
 LocalDateTime getUpdated();
 DocumentType getType();
 
 @Value.Immutable @JsonSerialize(as = ImmutableServiceRevisionDocument.class) @JsonDeserialize(as = ImmutableServiceRevisionDocument.class)
 interface ServiceRevisionDocument extends ServiceDocument {
   String getHead(); //latest proccess id
   String getName();
   List<ServiceRevisionValue> getValues();
   @Value.Default
   default DocumentType getType() { return DocumentType.SERVICE_REV; }

   @JsonIgnore
   default String getHeadDefId() {
     return getValues().stream().filter(e -> e.getId().equals(getHead())).map(e -> e.getProcessDocumentId()).findFirst().get();
   }
   
 }
 
 @Value.Immutable @JsonSerialize(as = ImmutableServiceDefinitionDocument.class) @JsonDeserialize(as = ImmutableServiceDefinitionDocument.class)
 interface ServiceDefinitionDocument extends ServiceDocument {
   List<RefIdValue> getRefs(); // stencil and wrench
   List<ProcessValue> getProcesses();
   @Value.Default
   default DocumentType getType() { return DocumentType.SERVICE_DEF; }
   @JsonIgnore
   default RefIdValue getHdes() { return this.getRefs().stream().filter(e -> e.getType().equals(ConfigType.HDES)).findFirst().get(); }
   @JsonIgnore
   default RefIdValue getStencil() { return this.getRefs().stream().filter(e -> e.getType().equals(ConfigType.STENCIL)).findFirst().get(); }
 }

 @Value.Immutable @JsonSerialize(as = ImmutableServiceReleaseDocument.class) @JsonDeserialize(as = ImmutableServiceReleaseDocument.class)
 interface ServiceReleaseDocument extends ServiceDocument {
   LocalDateTime getActiveFrom();
   String getRepoId();
   String getName();
   List<String> getDesc();
   List<ServiceReleaseValue> getValues();
   @Value.Default
   default DocumentType getType() { return DocumentType.SERVICE_RELEASE; }
 }
 
 @Value.Immutable @JsonSerialize(as = ImmutableServiceConfigDocument.class) @JsonDeserialize(as = ImmutableServiceConfigDocument.class)
 interface ServiceConfigDocument extends ServiceDocument {
   ServiceConfigValue getStencil();
   ServiceConfigValue getDialob();
   ServiceConfigValue getHdes();
   ServiceConfigValue getService();

   @Value.Default
   default DocumentType getType() { return DocumentType.SERVICE_CONFIG; }
 }


 @Value.Immutable @JsonSerialize(as = ImmutableProcessValue.class) @JsonDeserialize(as = ImmutableProcessValue.class)
 interface ProcessValue extends Serializable {
   @Nullable String getId();
   String getName();
   String getDesc();
   String getFlowId();
   String getFormId();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableRefIdValue.class) @JsonDeserialize(as = ImmutableRefIdValue.class)
 interface RefIdValue extends Serializable {
   @Nullable String getId();
   String getTagName();
   String getRepoId();
   ConfigType getType(); 
 }
 @Value.Immutable @JsonSerialize(as = ImmutableServiceConfigValue.class) @JsonDeserialize(as = ImmutableServiceConfigValue.class)
 interface ServiceConfigValue extends Serializable {
   String getId();
   ConfigType getType();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableServiceRevisionValue.class) @JsonDeserialize(as = ImmutableServiceRevisionValue.class)
 interface ServiceRevisionValue extends Serializable {
   String getId();
   String getRevisionName(); // iteration X
   String getProcessDocumentId();
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
