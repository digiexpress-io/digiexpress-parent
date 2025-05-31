package io.digiexpress.thena.batch.client.spi.persistence.sql;

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

import io.digiexpress.thena.batch.client.api.entities.ImmutableEnvelopeLog;
import io.digiexpress.thena.batch.client.api.entities.Envelope.OperationStatus;
import io.digiexpress.thena.batch.client.api.persistence.ImmutableBatchTransactionEntries;
import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;


public class BatchTransactionException extends RuntimeException {
  private static final long serialVersionUID = -7251738425609399151L;
  private final BatchTransactionEntries batch;
  
  public BatchTransactionException(BatchTransactionEntries current, String msg, Throwable t) {
    this.batch = ImmutableBatchTransactionEntries.builder()
        .from(current)
        .status(OperationStatus.ERROR)
        .addMessages(ImmutableEnvelopeLog.builder().text(msg).exception(t).build())
        .addMessages(ImmutableEnvelopeLog.builder().text(t.getMessage()).build())
        .build(); 
  }
  
  public BatchTransactionException(BatchTransactionEntries batch) {
    this.batch = batch;
  }
  public BatchTransactionEntries getBatch() {
    return batch;
  }
}
