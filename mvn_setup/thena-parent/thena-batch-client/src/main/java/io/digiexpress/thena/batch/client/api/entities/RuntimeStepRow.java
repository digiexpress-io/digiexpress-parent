package io.digiexpress.thena.batch.client.api.entities;

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
import java.util.Optional;

import org.immutables.value.Value;

import io.digiexpress.thena.batch.client.api.entities.RuntimeInstance.RuntimeExecutionStatus;
import io.vertx.core.json.JsonObject;


@Value.Immutable
public interface RuntimeStepRow extends AnyBatchEntity {
  String getId();
  String getRuntimeId();
  String getStepId();
  
  RuntimeExecutionStatus getExecutionStatus();
  
  OffsetDateTime getCreatedAt();
  Optional<OffsetDateTime> getEndedAt();
  
  Long getRowNumber();
  String getExternalId();
  
  Optional<JsonObject> getInput();
  Optional<JsonObject> getOutput();

  
  @Override 
  default public BatchDocType getDocType() { 
    return BatchDocType.RUNTIME_STEP_ROW; 
  }
}
