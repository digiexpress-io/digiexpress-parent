package io.digiexpress.client.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface ServiceDocument {
 enum DocumentType { PROCESS_REV, PROCESS_DEF, SERVICE_CONFIG, SERVICE_RELEASE }
 enum ConfigType { STENCIL, DIALOB, WRENCH, SERVICE }
 
 @Nullable String getId(); // unique id
 @Nullable String getVersion(); // not really nullable, just in serialization
 
 DocumentType getType();
 LocalDateTime getCreated();
 LocalDateTime getUpdated();
 
 
 @Value.Immutable @JsonSerialize(as = ImmutableProcessRevisionDocument.class) @JsonDeserialize(as = ImmutableProcessRevisionDocument.class)
 interface ProcessRevisionDocument extends ServiceDocument {
   String getHead(); //latest proccess id
   String getName();
   List<ProcessRevisionValue> getValues();
   @Value.Default
   default DocumentType getType() { return DocumentType.PROCESS_REV; }
 }
 
 @Value.Immutable @JsonSerialize(as = ImmutableProcessDocument.class) @JsonDeserialize(as = ImmutableProcessDocument.class)
 interface ProcessDocument extends ServiceDocument {
   String getProcessName();
   List<RefIdValue> getValues();
   Boolean getDevMode();
   @Value.Default
   default DocumentType getType() { return DocumentType.PROCESS_DEF; }
 }
 
 @Value.Immutable @JsonSerialize(as = ImmutableServiceReleaseDocument.class) @JsonDeserialize(as = ImmutableServiceReleaseDocument.class)
 interface ServiceReleaseDocument extends ServiceDocument {
   String getRepoId();
   String getReleaseName();
   @Nullable String getReleaseDescription();
   LocalDateTime getActiveFrom();
   
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

 

 
 @Value.Immutable @JsonSerialize(as = ImmutableRefIdValue.class) @JsonDeserialize(as = ImmutableRefIdValue.class)
 interface RefIdValue extends Serializable {
   @Nullable String getId();
   String getRefName();
   String getRefRevName();
   ConfigType getType(); 
 }
 @Value.Immutable @JsonSerialize(as = ImmutableServiceConfigValue.class) @JsonDeserialize(as = ImmutableServiceConfigValue.class)
 interface ServiceConfigValue extends Serializable {
   String getId();
   ConfigType getType();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableProcessRevisionValue.class) @JsonDeserialize(as = ImmutableProcessRevisionValue.class)
 interface ProcessRevisionValue extends Serializable {
   String getId();
   String getRevisionName();
   String getProcessDocumentId();
   LocalDateTime getCreated();
   LocalDateTime getUpdated();
 }
 @Value.Immutable @JsonSerialize(as = ImmutableServiceReleaseValue.class) @JsonDeserialize(as = ImmutableServiceReleaseValue.class)
 interface ServiceReleaseValue extends Serializable {
   ConfigType getId();
   String getBodyHash();
   String getBody();
 }

}
