package io.digiexpress.eveli.client.web.resources.worker;

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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import org.immutables.value.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.digiexpress.eveli.client.api.WorkerAuthClient;
import io.digiexpress.eveli.client.config.EveliPropsBatch;
import io.digiexpress.thena.batch.client.api.BatchClient;
import io.digiexpress.thena.batch.client.api.entities.Batch;
import io.digiexpress.thena.batch.client.api.entities.BatchConfig;
import io.digiexpress.thena.batch.client.api.entities.Envelope.EnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeStep;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/worker/rest/api/batches")
@RequiredArgsConstructor
@Slf4j
public class BatchApiCotroller {
  private final WorkerAuthClient auth;
  private final BatchClient batchClient;
  private final Supplier<BatchConfig> batchConfig;
  private final EveliPropsBatch batchProps;
  private final ApplicationEventPublisher publisher;
  
  @JsonSerialize(as = ImmutableCreateOneInstanceCommand.class)
  @JsonDeserialize(as = ImmutableCreateOneInstanceCommand.class)
  @Value.Immutable
  public interface CreateOneInstanceCommand {
    String getInstanceName();
    String getCommitMessage();
    JsonObject getParams();
  }
  
  public static class BatchApiException extends RuntimeException {
    private static final long serialVersionUID = 6289034776932458489L;
    
    public BatchApiException(String msg, List<EnvelopeLog> logs) {
      super(
          msg + System.lineSeparator() +  
          String.join(System.lineSeparator(), logs.stream().map(e -> e.getText()).toList())
      );
      
      logs.stream()
        .filter(e -> e.getException() != null)
        .map(e -> e.getException())
        .forEach(l -> this.addSuppressed(l));
    }
  }
  
  
  @GetMapping
  public Multi<Batch> findAllBatches() {
    return batchClient.queryBatches().findAll()
        .onItem().transformToMulti(resp -> {
          if(resp.getOperationStatus() == OperationStatus.OK) {
            return Multi.createFrom().items(resp.getObject().stream());
          }
          throw new BatchApiException("Failed to find any batches", resp.getOperationLogs());
        });
  }
  
  @GetMapping("/{batchName}")
  public Uni<Batch> getBatches(@PathVariable("batchName") String batchName) {
    return batchClient.queryBatches().getOne(batchName).onItem().transform(resp -> resp.getObject());
  }
  
  @GetMapping("/steps/{stepId}")
  public Uni<RuntimeStep> getBatchInstanceStep(@PathVariable("stepId") String stepId) {
    return batchClient.queryRuntimeSteps().getOne(stepId).onItem().transform(resp -> resp.getObject());
  }
  
  @PostMapping("/{batchName}/instances")
  public Uni<RuntimeInstance> createOneInstance(
      @PathVariable("batchName") String batchName, 
      @RequestBody CreateOneInstanceCommand body) {

    return batchClient.createOneRuntimeInstance()
        .batchName(batchName)
        .appId(batchProps.getAppId())
        .commitAuthor(auth.getUser().getPrincipal().getUsername())
        .commitMessage(body.getCommitMessage())
        .instanceName(body.getInstanceName())
        .params(body.getParams())
        .instanceSeq(true)
        .build()
        .onItem().transform(env -> {
          if(env.getOperationStatus() == OperationStatus.OK) {
            return env.getObject();
          }
          throw new BatchApiException(
              "Failed to create batch instance for: " + batchName + ", json: " + JsonObject.mapFrom(body).encode(), 
              env.getOperationLogs());
        })
        .onItem().invoke(instance -> publisher.publishEvent(new RuntimeInstanceCreatedEvent(instance)));
  }
  
  @RequiredArgsConstructor @Data
  public class RuntimeInstanceCreatedEvent {
    private final RuntimeInstance instance;
  }
  
  @Async
  @EventListener
  public CompletableFuture<?> handleStartAndRunInstance(RuntimeInstanceCreatedEvent event) {  
    try {
      
      final var envir = batchClient.createBatchEnvir()
          .config(batchConfig.get())
          .build();
        
      return envir
        .executor()
        .commitMessage("Running with Spring @Async @EventListener")
        .commitAuthor(BatchApiCotroller.class.getSimpleName())
        .execute(event.getInstance())
        .subscribeAsCompletionStage().toCompletableFuture();

    } catch(Exception e) {
      log.error("Failed to check to run instance: {}!", JsonObject.mapFrom(event).encode());
      return Uni.createFrom().voidItem().subscribeAsCompletionStage().toCompletableFuture();
    }
  }
}
