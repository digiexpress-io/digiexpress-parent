package io.digiexpress.eveli.client.api;

import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface AttachmentCommands {
  AttachmentQuery query();
  AttachmentUploadBuilder upload();
  AttachmentUrlBuilder url();
  
  interface AttachmentQuery {
    List<Attachment> processId(String processId);
    List<Attachment> taskId(String taskId);
  }
  
  interface AttachmentUrlBuilder {
    AttachmentUrlBuilder filename(String filename);
    AttachmentUrlBuilder encodePath(String filename);
    Optional<URL> taskId(String taskId) throws URISyntaxException;
    Optional<URL> processId(String processId) throws URISyntaxException;
  }
  
  interface AttachmentUploadBuilder {
    AttachmentUploadBuilder filename(String filename);
    AttachmentUploadBuilder encodePath(String filename);
    Optional<AttachmentUpload> taskId(String taskId);
    Optional<AttachmentUpload> processId(String processId);
  }
  

  @Value.Immutable
  @JsonSerialize(as = ImmutableAttachmentUpload.class)
  @JsonDeserialize(as = ImmutableAttachmentUpload.class)
  interface AttachmentUpload {
    String getPutRequestUrl();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableAttachment.class)
  @JsonDeserialize(as = ImmutableAttachment.class)
  interface Attachment {
    String getName();
    ZonedDateTime getCreated();
    ZonedDateTime getUpdated();
    Long getSize();
    AttachmentStatus getStatus();
  }
  
  enum AttachmentStatus { OK, QUARANTINED, UPLOADED }
}