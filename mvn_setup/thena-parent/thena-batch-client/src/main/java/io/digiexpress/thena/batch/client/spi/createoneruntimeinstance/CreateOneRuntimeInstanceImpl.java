package io.digiexpress.thena.batch.client.spi.createoneruntimeinstance;

/*-
 * #%L
 * thena-batch-client
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

import java.time.OffsetDateTime;

import io.digiexpress.thena.batch.client.api.BatchClient.CreateOneRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.Envelope;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelope;
import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.ImmutableRuntimeParams;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeStatus;
import io.digiexpress.thena.batch.client.api.persistence.BatchDb;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;
import io.digiexpress.thena.batch.client.spi.createbatchconfig.CreateBatchConfigException;
import io.digiexpress.thena.batch.client.spi.createoneruntimeinstance.CORI_Logger.CRI_LogEventType;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.experimental.Accessors;


@Data @Accessors(fluent = true)
public class CreateOneRuntimeInstanceImpl implements CreateOneRuntimeInstance {
  private final CORI_Logger logger;
  private final BatchDb batchDb;

  
  private String appId;
  private String batchName;
  private String instanceName;
  private boolean instanceSeq;
  private String commitMessage;
  private String commitAuthor;
  private JsonObject params;
  
  public CreateOneRuntimeInstanceImpl(BatchDb batchDb) {
    this.batchDb = batchDb;
    this.logger = new CORI_Logger();
  }
  
  @Override
  public Uni<Envelope<RuntimeInstance>> build() {
    
    RepoAssert.notEmpty(batchName, () -> "batchName must be defined!");
    RepoAssert.notEmpty(appId, () -> "appId must be defined!");
    RepoAssert.notEmpty(instanceName, () -> "instanceName must be defined!");
    RepoAssert.notEmpty(commitMessage, () -> "commitMessage must be defined!");
    RepoAssert.notEmpty(commitAuthor, () -> "commitAuthor must be defined!");
    
    return batchDb.withTenant().onItem().transformToUni(db -> execute(db));
  }
  
  public Uni<Envelope<RuntimeInstance>> execute(BatchDb batchDb) {
    final var scope = ImmutableTxScope.builder()
        .tenantId(batchDb.getDataSource().getTenant().getId())
        .commitAuthor(commitAuthor)
        .commitMessage(commitMessage)
        .build();

    
    return batchDb
        .withTransaction(scope, db -> {
          return createRequest(db)
              .onItem().transformToUni(request -> db.builder().persist(request))
              .onItem().transform(persisted -> createResponse(persisted, db));
        })
        .onFailure().recoverWithItem(throwable -> {
          logger.fail(throwable);
          return ImmutableEnvelope.<RuntimeInstance>builder()
            .tenantId(scope.getTenantId())
            .addOperationLogs(ImmutableEnvelopeLog.builder()
                .text(new StringBuilder()
                  .append("Runtime instance commit to: '").append(scope.getTenantId()).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(throwable.getMessage())
                  .toString())
                .exception(throwable)
                .build())
            .operationStatus(OperationStatus.ERROR)
          .build();
        })
        .invoke(envelope -> {
          logger.close();
        });
  }
  

  private Envelope<RuntimeInstance> createResponse(BatchTransactionEntries rsp, BatchDb db) {
    if(rsp.getStatus() == OperationStatus.CONFLICT || rsp.getStatus() == OperationStatus.ERROR) {
      throw new CreateBatchConfigException("Failed to create runtime instance!", rsp);
    }
    
    final Envelope<RuntimeInstance> result = ImmutableEnvelope.<RuntimeInstance>builder()
        .tenantId(db.getDataSource().getTenant().getId())
        .operationStatus(rsp.getStatus())
        .object(rsp.getRuntimeInstanceInserts().iterator().next())
        .build();
    return result;
  }
  
  private Uni<BatchTransactionEntries> createRequest(BatchDb batchDb) {
    
    return Uni.combine().all().unis(
        batchDb.query().queryBatches().findOneByAppIdAndName(appId, batchName), 
        instanceSeq ? batchDb.query().queryInstances().nextSequence() : Uni.createFrom().item(0l))
    .asTuple().onItem().transform(tuple -> {
      
      
      RepoAssert.isTrue(tuple.getItem1().isPresent(), () -> {
        logger.append(CRI_LogEventType.BATCH_NOT_FOUND, JsonObject
            .of(
                "appId", appId,
                "batchName", batchName
            ));
        return "Can't find batch by name: '" + batchName + "'!";  
      });
      
      
      final var ctx = CORI_Context.builder()
          .appId(this.appId)
          .userId(this.commitAuthor)
          .logger(this.logger)
          .tenantId(batchDb.getDataSource().getTenant().getId())
          .now(OffsetDateTime.now())
          .build();
      
      final var txEntries = ctx.createPersistContainer()
          .addCommitMessages(this.commitMessage)
          .addCommitAuthors(this.commitAuthor);

      
      
      final var suffix = this.instanceSeq ? "-" + tuple.getItem2().toString() : "";
      
      final var instance = ImmutableRuntimeInstance.builder()
          .id(OidUtils.gen())
          .batchId(tuple.getItem1().get().getId())
          .name(instanceName + suffix)
          .comment(commitMessage)
          .createdAt(ctx.getNow())
          .status(RuntimeStatus.CREATED)
          .executionStatus(RuntimeExecutionStatus.OK)
          .build();
      txEntries.addRuntimeInstanceInserts(instance);
      logger.append(instance, CRI_LogEventType.RUNTIME_INSTANCE_CREATED);
    
      if(params != null) {
        final var params = ImmutableRuntimeParams.builder()
            .id(OidUtils.gen())
            .comment("created with instance as start up params")
            .body(this.params)
            .name("start-up")
            .createdAt(ctx.getNow())
            .runtimeId(instance.getId())
            .build();
        txEntries.addRuntimeParamInserts(params);
        logger.append(params, CRI_LogEventType.RUNTIME_INSTANCE_PARAMS_CREATED);
      }
      

      return txEntries.build();
    });
  }

}
