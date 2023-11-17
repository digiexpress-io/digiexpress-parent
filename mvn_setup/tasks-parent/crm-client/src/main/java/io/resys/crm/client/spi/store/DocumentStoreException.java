package io.resys.crm.client.spi.store;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.thena.docdb.api.actions.CommitActions.CommitResultEnvelope;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObject;
import io.resys.thena.docdb.api.models.ThenaGitObjects.PullObjects;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;



public class DocumentStoreException extends RuntimeException {

  private static final long serialVersionUID = 7058468238867536222L;

  private final String code;
  private final JsonObject target;
  private final List<DocumentExceptionMsg> messages = new ArrayList<>();
  
  @Value.Immutable @JsonSerialize(as = ImmutableDocumentExceptionMsg.class)
  public interface DocumentExceptionMsg {
    String getId();
    String getValue();
    List<String> getArgs();
  }
  
  public DocumentStoreException(String code, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, null, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = null;
  }
  public DocumentStoreException(String code, JsonObject target, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, target, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = target;
  }

  public String getCode() { return code; }
  public List<DocumentExceptionMsg> getMessages() { return messages; }
  public JsonObject getTarget() { return target; }  
  
 
  public static DocumentExceptionMsg convertMessages(CommitResultEnvelope commit) {
    return ImmutableDocumentExceptionMsg.builder()
        .id(commit.getGid())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  public static DocumentExceptionMsg convertMessages(ManyDocsEnvelope envelope) {
    return ImmutableDocumentExceptionMsg.builder()
        .id(envelope.getRepoId())
        .value("") //TODO
        .addAllArgs(envelope.getMessages().stream().map(message-> message.getText()).collect(Collectors.toList()))
        .build();
  }
  
  
  public static DocumentExceptionMsg convertMessages1(QueryEnvelope<PullObject> state) {
    return ImmutableDocumentExceptionMsg.builder()
        .id("STATE_FAIL")
        .value("")
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }
  public static DocumentExceptionMsg convertMessages2(QueryEnvelope<PullObjects> state) {
    return ImmutableDocumentExceptionMsg.builder()
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  public static Builder builder(String msgId) {
    return new Builder(msgId);
  }
  
  @RequiredArgsConstructor
  public static class Builder {
    private final String id;
    private final ImmutableDocumentExceptionMsg.Builder msg = ImmutableDocumentExceptionMsg.builder();
    
    public Builder add(DocumentConfig config, QueryEnvelope<?> envelope) {
      msg.id(envelope.getRepo() == null ? config.getRepoId() : envelope.getRepo().getName())
      .value(envelope.getRepo() == null ? "no-repo" : envelope.getRepo().getId())
      .addAllArgs(envelope.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()));
      return this;
    }
    public Builder add(Consumer<ImmutableDocumentExceptionMsg.Builder> callback) {
      callback.accept(msg);
      return this;
    }
    
    public DocumentStoreException build() {
      return new DocumentStoreException(id, msg.build());
    }
  }
}
