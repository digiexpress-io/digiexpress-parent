package io.resys.thena.tasks.client.thenamission.visitors;

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

import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.spi.ExMessageFormatter;
import io.resys.thena.spi.ExMessageFormatter.DocumentExceptionMsg;
import io.resys.thena.spi.ImmutableDocumentExceptionMsg;
import io.resys.thena.support.OidUtils;
import io.resys.thena.tasks.client.thenagit.store.DocumentConfig;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;



public class TaskStoreException extends RuntimeException {

  private static final long serialVersionUID = 7058468238867536222L;

  private final String code;
  private final JsonObject target;
  private final List<DocumentExceptionMsg> messages = new ArrayList<>();
  
  
  public TaskStoreException(String code, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, null, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = null;
  }
  public TaskStoreException(String code, JsonObject target, DocumentExceptionMsg ... msg) {
    super(new ExMessageFormatter(code, target, msg).format());
    this.code = code;
    this.messages.addAll(Arrays.asList(msg));
    this.target = target;
  }

  public String getCode() { return code; }
  public List<DocumentExceptionMsg> getMessages() { return messages; }
  public JsonObject getTarget() { return target; }  
  
 
  public static DocumentExceptionMsg convertMessages(ManyMissionsEnvelope commit) {
    return ImmutableDocumentExceptionMsg.builder()
        .id(OidUtils.gen())
        .value("") //TODO
        .addAllArgs(commit.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }

  public static DocumentExceptionMsg convertMessages1(QueryEnvelope<GitPullActions.PullObject> state) {
    return ImmutableDocumentExceptionMsg.builder()
        .id("STATE_FAIL")
        .value("")
        .addAllArgs(state.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()))
        .build();
  }
  public static DocumentExceptionMsg convertMessages2(QueryEnvelope<GitPullActions.PullObjects> state) {
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
      msg.id(envelope.getRepo() == null ? config.getProjectName() : envelope.getRepo().getName())
      .value(envelope.getRepo() == null ? "no-repo" : envelope.getRepo().getId())
      .addAllArgs(envelope.getMessages().stream().map(message->message.getText()).collect(Collectors.toList()));
      return this;
    }
    public Builder add(Consumer<ImmutableDocumentExceptionMsg.Builder> callback) {
      callback.accept(msg);
      return this;
    }
    
    public TaskStoreException build() {
      return new TaskStoreException(id, msg.build());
    }
  }
}
