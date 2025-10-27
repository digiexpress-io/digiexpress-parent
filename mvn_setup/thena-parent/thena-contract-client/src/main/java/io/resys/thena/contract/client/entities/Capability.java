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

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCapability.class)
@JsonDeserialize(as = ImmutableCapability.class)
public interface Capability extends ContractEntity {
  UUID getId();
  UUID getContractId();

  Optional<String> getExternalId();
  UUID getCommitId();
  UUID getCreatedCommitId();

  // Transitive data from joins
  @Value.Auxiliary
  @Nullable CapabilityTransitives getTransitives();

  String getCapabilityCode();
  String getCapabilityName();
  String getCapabilityType();
  Boolean getCapabilityEnabled();

  @Override 
  default ContractDocType getDocType() { 
    return ContractDocType.CAPABILITY; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCapabilityTransitives.class)
  @JsonDeserialize(as = ImmutableCapabilityTransitives.class)
  interface CapabilityTransitives {
    OffsetDateTime getCreatedAt();
    OffsetDateTime getUpdatedAt();
  }
}
