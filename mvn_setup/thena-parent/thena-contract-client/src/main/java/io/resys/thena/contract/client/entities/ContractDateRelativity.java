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
import java.time.Period;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableContractDateRelativity.class)
@JsonDeserialize(as = ImmutableContractDateRelativity.class)
public interface ContractDateRelativity extends ContractEntity {
  String getId();
  String getContractId();
  
  // Polymorphic FKs - only one should be populated
  Optional<String> getInvPlanId();
  Optional<String> getCoverageId();
  Optional<String> getPartyId();
  Optional<String> getPaymentPlanId();
  
  String getEntityType();
  String getFieldName();
  
  // Relativity rule
  String getRelativeToType();
  Optional<Period> getOffsetInterval();
  Optional<String> getCalculationRule();
  
  Optional<String> getDescription();
  OffsetDateTime getCreatedAt();
}