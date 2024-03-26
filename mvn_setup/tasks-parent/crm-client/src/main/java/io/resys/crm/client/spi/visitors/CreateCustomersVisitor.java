package io.resys.crm.client.spi.visitors;

/*-
 * #%L
 * thena-Projects-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CreateCustomer;
import io.resys.crm.client.api.model.Document;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocCreateVisitor;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.visitors.CustomerCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateCustomersVisitor implements DocCreateVisitor<Customer> {
  private final List<? extends CreateCustomer> commands;
  private final List<Customer> customers = new ArrayList<Customer>();
  
  @Override
  public CreateManyDocs start(DocumentConfig config, CreateManyDocs builder) {
    builder
      .docType(Document.DocumentType.CUSTOMER.name())
      .author(config.getAuthor().get())
      .message("creating customer");
    
    for(final var command : commands) {
      try {
        final var entity = new CustomerCommandVisitor(config).visitTransaction(Arrays.asList(command));
        final var json = JsonObject.mapFrom(entity);
        builder.item()
          .append(json)
          .docId(entity.getId())
          .externalId(entity.getExternalId())
          .next();
        customers.add(entity);
      } catch (NoChangesException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    return builder;
  }

  @Override
  public List<DocBranch> visitEnvelope(DocumentConfig config, ManyDocsEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope.getBranch();
    }
    throw new DocumentStoreException("CUSTOMER_CREATE_FAIL", DocumentStoreException.convertMessages(envelope));
  }

  @Override
  public List<Customer> end(DocumentConfig config, List<DocBranch> branches) {
    final Map<String, Customer> configsById = new HashMap<>(
        this.customers.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    
    branches.forEach(branch -> {
      
      final var next = ImmutableCustomer.builder()
          .from(configsById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      configsById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
  }

}
