package io.resys.thena.contract.client.entities;

/*-
 * #%L
 * thena-contract-client
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCommand.class)
@JsonDeserialize(as = ImmutableCommand.class)
public interface Command extends ContractEntity {
  String getId();
  String getContractId();

  Optional<String> getExternalId();
  String getCommitId();
  String getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable CommandTransitives getTransitives();

  JsonObject getCommandBody();
  String getCommandStatus();
  String getCommandType();
  Optional<LocalDate> getCommandTargetDate();
  Optional<String> getCommandDescription();
  Optional<JsonObject> getCommandError();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.COMMAND; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCommandTransitives.class)
  @JsonDeserialize(as = ImmutableCommandTransitives.class)
  interface CommandTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}