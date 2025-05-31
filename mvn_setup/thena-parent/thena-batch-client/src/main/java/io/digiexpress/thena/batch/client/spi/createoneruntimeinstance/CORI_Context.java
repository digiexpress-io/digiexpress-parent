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

import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor @Data @Builder
public class CORI_Context {

  private final OffsetDateTime now;
  private final String userId;
  private final String appId;
  private final String tenantId;

  private final CORI_Logger logger;
  
  

  public ImmutableBatchTransactionEntries.Builder createPersistContainer() {
    return ImmutableBatchTransactionEntries.builder()
        .tenantId(tenantId)
        .status(OperationStatus.OK)
        .log("");
  }
}
