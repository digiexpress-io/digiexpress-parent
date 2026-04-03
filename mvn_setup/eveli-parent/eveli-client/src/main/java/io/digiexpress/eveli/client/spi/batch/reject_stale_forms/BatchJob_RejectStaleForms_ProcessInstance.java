package io.digiexpress.eveli.client.spi.batch.reject_stale_forms;

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

import java.time.OffsetDateTime;
import java.util.Optional;

import io.dialob.api.questionnaire.Questionnaire;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.spi.batch.reject_stale_forms.BatchJob_RejectStaleForms_ProcessInstance.ProcAndQuestionnaireToReject;
import io.digiexpress.eveli.client.spi.batch.reject_stale_forms.BatchJob_RejectStaleForms_ProcessInstance.RejectStaleFormsConfig;
import io.digiexpress.thena.batch.client.api.executor.Executor;
import io.digiexpress.thena.batch.client.api.executor.ExecutorConfig;
import io.digiexpress.thena.batch.client.api.executor.ExecutorContext;
import io.digiexpress.thena.batch.client.api.executor.ExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ExecutorQuery;
import io.digiexpress.thena.batch.client.api.executor.ExecutorResult;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorEntity;
import io.digiexpress.thena.batch.client.api.executor.ImmutableExecutorResult;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BatchJob_RejectStaleForms_ProcessInstance implements Executor<ProcAndQuestionnaireToReject, RejectStaleFormsConfig> {

  private final TaskClient taskClient;
  private final FormDb dialobClient;

  @Override
  public ExecutorQuery<ProcAndQuestionnaireToReject, RejectStaleFormsConfig> before(ExecutorContext context) {
    return new ExecutorQuery<ProcAndQuestionnaireToReject, RejectStaleFormsConfig>() {
      @Override
      public RejectStaleFormsConfig getConfig() {
        // 6 months old tasks
        return new RejectStaleFormsConfig(6);
      }
      @Override
      public Multi<ProcAndQuestionnaireToReject> findAll() {
        final var config = getConfig();
        return taskClient.queryTaskProcesess()
            .findAllStaleWithoutTasks(OffsetDateTime.now().minusMonths(config.getAgeInMonths()))
            .onItem().transform(proc -> {
              try {
                final var questionnaire = dialobClient.withTenant().formInstanceQuery().findOneSync(proc.getQuestionnaireId()).map(e -> e.getQuestionnaire());
                return new ProcAndQuestionnaireToReject(proc, questionnaire);
              } catch(Exception e) {
                return new ProcAndQuestionnaireToReject(proc, Optional.empty());                
              }
            }).filter(e -> {

              if(e.getQuestionnaire().isEmpty()) {
                return true;
              }

              final var updated = e.getQuestionnaire().get().getMetadata().getLastAnswer();
              return updated.compareTo(e.getProcess().getUpdated().toInstant()) <= 0;
            });
      }
    };
  }

  @Override
  public Uni<ExecutorEntity> accept(ProcAndQuestionnaireToReject entity, RejectStaleFormsConfig config, ExecutorContext context) {
    return taskClient.modifyProcess()
        .commitAuthor(BatchJob_RejectStaleForms_ProcessInstance.class.getSimpleName())
        .commitMessage("Older then: " + config.getAgeInMonths() + " months")
        .id(entity.getProcess().getId().toString())
        .merge((current, merger) -> merger.status(GrimProcessStatus.EXPIRED).build())
        .build()
        .onItem().transform((rejected) -> {
          
          
          return ImmutableExecutorEntity.builder()  
              .status(ExecutorEntity.ExecutorEntityStatus.OK)
              .entityId(
                  "process instance id: " + entity.getProcess().getId() + ", " + 
                      "dialob session: " + entity.getQuestionnaire().map(e -> e.getId()).orElse(null)
                  )
              .inputBody(JsonObject.mapFrom(entity))
              .build();
        }); 
  }

  @Override
  public Uni<ExecutorResult> after(RejectStaleFormsConfig config, ExecutorContext context) {
    return Uni.createFrom().item(ImmutableExecutorResult.builder()
        .status(ExecutorResult.ExecutorStatus.OK)
        .build());
  }

  @RequiredArgsConstructor
  @Data
  public static class RejectStaleFormsConfig implements ExecutorConfig {
    private static final long serialVersionUID = 7079554536966522627L;
    private final int ageInMonths;
  }


  @RequiredArgsConstructor @Data
  public static class ProcAndQuestionnaireToReject {
    private final ProcessInstance process;
    private final Optional<Questionnaire> questionnaire;
  }
}
