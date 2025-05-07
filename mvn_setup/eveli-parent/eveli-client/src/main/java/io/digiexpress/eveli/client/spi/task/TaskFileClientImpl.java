package io.digiexpress.eveli.client.spi.task;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.ImmutableList;

import io.digiexpress.eveli.client.api.AttachmentCommands;
import io.digiexpress.eveli.client.api.AttachmentCommands.Attachment;
import io.digiexpress.eveli.client.api.ImmutableTaskFile;
import io.digiexpress.eveli.client.api.TaskFileClient;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TaskFileClientImpl implements TaskFileClient {

  private final AttachmentCommands attachmentCommands;
  private final RestTemplate restTemplate;
  
  
  @Override
  public QueryTaskFiles queryTaskFiles() {
    return new QueryTaskFiles() {
      @Override
      public Uni<List<TaskFile>> findAll(String taskId) {
        return Multi.createFrom().iterable(ImmutableList.<AttachmentCommands.Attachment>builder()
            .addAll(attachmentCommands.query().processId(taskId))
            .addAll(attachmentCommands.query().taskId(taskId))
            .build())
        .onItem().transform(attachment -> createReq(attachment))
        .onItem().transform(req -> createResp(req))
        .collect().asList();
      }
    };
  }
  
  private TaskFile createResp(AttReq req) {
    final ResponseEntity<byte[]> resp = restTemplate.getForEntity(req.getUrl(), byte[].class);
    
    if(!resp.getStatusCode().is2xxSuccessful()) {
      throw new TaskFileClientException(
          "Failed to get task/process: " + req.getAttachment().getProcessId().orElse(req.getAttachment().getTaskId().orElse("")) + "\r\n"  + 
          " attachment with url: " + req.getUrl() + "\r\n" + 
          " attachment api resp status code: " + resp.getStatusCode()
          
      );
    }
    
    final var body = resp.getBody();
    if(body.length == 0) {
      throw new TaskFileClientException(
          "Failed to get task/process: " + req.getAttachment().getProcessId().orElse(req.getAttachment().getTaskId().orElse("")) + "\r\n"  + 
          " attachment with url: " + req.getUrl() + "\r\n" + 
          " attachment api resp status code: " + resp.getStatusCode() + "\r\n" + 
          " body size is 0 === ZERO bytes???!"
      );
    }
    
    final var headers = resp.getHeaders();
    return ImmutableTaskFile.builder()
        .mimeType(headers.getContentType().toString())
        .bodyType(headers.getContentType().toString())
        .body(new ByteArrayInputStream(body))
        .externalId(req.getUrl().toString())
        .name(req.getAttachment().getName())
        .build();
  }
  
  private AttReq createReq(AttachmentCommands.Attachment attachment) {
    try {
      final var url = attachment.getProcessId().isPresent() ?
        attachmentCommands.url().encodePath(attachment.getName()).processId(attachment.getProcessId().get()) : 
        attachmentCommands.url().encodePath(attachment.getName()).taskId(attachment.getTaskId().get());
      
      return new AttReq(url.get().toURI(), attachment);
    } catch(Exception ex1) {
      throw new RuntimeException(ex1.getMessage(), ex1);
    }
  }
  
  
  
  public static class TaskFileClientException extends RuntimeException {

    private static final long serialVersionUID = -8826220451814295169L;
    public TaskFileClientException(String message) {
      super(message);
    }
    public TaskFileClientException(String message, Throwable cause) {
      super(message, cause);
    }
    
  } 

  @RequiredArgsConstructor
  @Data
  private static class AttReq {
    private final URI url;
    private final Attachment attachment;
  }
}
