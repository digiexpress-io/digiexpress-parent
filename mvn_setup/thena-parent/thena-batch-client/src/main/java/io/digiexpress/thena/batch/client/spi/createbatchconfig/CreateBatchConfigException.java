package io.digiexpress.thena.batch.client.spi.createbatchconfig;

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

import io.digiexpress.thena.batch.client.api.persistence.BatchDbBuilder.BatchTransactionEntries;

public class CreateBatchConfigException extends RuntimeException {
  private static final long serialVersionUID = -6202574733069488724L;
  private final BatchTransactionEntries batch;
  public CreateBatchConfigException(String message, BatchTransactionEntries batch) {
    super(message + System.lineSeparator() + " " +
        String.join(System.lineSeparator() + " ", batch.getMessages().stream().map(e -> e.getText()).toList()));
    
    batch.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
    this.batch = batch;
  }
  public BatchTransactionEntries getBatch() {
    return batch;
  }
}
