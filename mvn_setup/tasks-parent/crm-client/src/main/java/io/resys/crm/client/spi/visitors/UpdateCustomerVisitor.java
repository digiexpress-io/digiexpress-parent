package io.resys.crm.client.spi.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.crm.client.spi.store.DocumentStore;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.store.MainBranch;
import io.resys.crm.client.spi.visitors.CustomerCommandVisitor.NoChangesException;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.docdb.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaDocObject.Doc;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocBranch;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocCommit;
import io.resys.thena.docdb.api.models.ThenaDocObject.DocLog;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateCustomerVisitor implements DocObjectsVisitor<Uni<List<Customer>>> {
  private final DocumentStore ctx;
  private final List<String> customerIds;
  private final ModifyManyDocBranches commitBuilder;
  private final Map<String, List<CustomerUpdateCommand>> commandsByCustomerId; 
  
  
  public UpdateCustomerVisitor(List<CustomerUpdateCommand> commands, DocumentStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByCustomerId = commands.stream()
        .collect(Collectors.groupingBy(CustomerUpdateCommand::getCustomerId));
    this.customerIds = new ArrayList<>(commandsByCustomerId.keySet());
    this.commitBuilder = config.getClient().doc().commit().modifyManyBranches()
        .repoId(config.getRepoId())
        .message("Update customers: " + commandsByCustomerId.size())
        .author(config.getAuthor().get());
  }

  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery builder) {
    return builder.matchIds(customerIds).branchName(MainBranch.HEAD_NAME);
  }

  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("GET_CUSTOMERS_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocumentStoreException.builder("GET_CUSTOMERS_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    if(customerIds.size() != result.getDocs().size()) {
      throw new DocumentStoreException("CUSTOMERS_UPDATE_FAIL_NOT_ALL_CUSTOMERS_FOUND", JsonObject.of("failedUpdates", customerIds));
    }
    return result;
  }

  @Override
  public Uni<List<Customer>> end(DocumentConfig config, DocObjects blob) {
    final var updatedTenants = blob.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {
      
      final var start = docBranch.getValue().mapTo(ImmutableCustomer.class);
      final var commands = commandsByCustomerId.get(start.getId());
      
      try {
        final var updated = new CustomerCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
        this.commitBuilder.item()
          .branchName(updated.getId())
          .append(JsonObject.mapFrom(updated));
        
        return updated;
      } catch(NoChangesException e) {
        return start;
      }
    });
    
    return commitBuilder.build().onItem().transform(response -> {
      if(response.getStatus() != CommitResultStatus.OK) {
        final var failedUpdates = customerIds.stream().collect(Collectors.joining(",", "{", "}"));
        throw new DocumentStoreException("CUSTOMERS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocumentStoreException.convertMessages(response));
      }
      
      final Map<String, Customer> configsById = new HashMap<>(
          updatedTenants.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
      
      
      response.getCommit().forEach(commit -> {
        
        final var next = ImmutableCustomer.builder()
            .from(configsById.get(commit.getDocId()))
            .version(commit.getId())
            .build();
        configsById.put(next.getId(), next);
      });
      
      return Collections.unmodifiableList(new ArrayList<>(configsById.values()));
    });
  }
}
