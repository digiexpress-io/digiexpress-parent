package io.resys.limaone.spi.dialob.builders;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Objects;

import io.dialob.api.proto.Actions;
import io.resys.limaone.spi.dialob.FormDb.FormInstance;
import io.resys.limaone.spi.dialob.FormDb.MergeFormInstance;
import io.resys.limaone.spi.dialob.FormDbImpl.FormDbProps;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MergeFormInstanceImpl implements MergeFormInstance {
  private final FormDbProps db;
  private String questionnaireId;
  private Actions actions;
  private Boolean forceCompletion;

  @Override
  public MergeFormInstance formInstanceId(String questionnaireId) {
    this.questionnaireId = Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    return this;
  }
  @Override
  public MergeFormInstance props(Actions actions) {
    this.actions = Objects.requireNonNull(actions, () -> "actions must be defined");
    return this;
  }
  @Override
  public MergeFormInstance forceCompletion(boolean forceCompletion) {
    this.forceCompletion = forceCompletion;
    return this;
  }

  @Override
  public Uni<FormInstance> build() {
    Objects.requireNonNull(questionnaireId, () -> "questionnaireId must be defined");
    
    if (Boolean.TRUE.equals(forceCompletion)) {
      return forceCompleteSession();
    }
    
    Objects.requireNonNull(actions, () -> "actions must be defined when not force completing");
    
    // Apply normal actions to questionnaire
    return new FormFillBuilderImpl(db)
        .actions(JsonObject.mapFrom(actions).encode()).build()
        .onItem().transformToUni((ignore) -> new FormInstanceQueryImpl(db).getOne(questionnaireId));
  }
  
  private Uni<FormInstance> forceCompleteSession() {
    return new FormFillBuilderImpl(db)
        .actions(JsonObject.mapFrom(actions).encode()).build()
        .onItem().transformToUni((ignore) -> new FormInstanceQueryImpl(db).getOne(questionnaireId));
  }
}
