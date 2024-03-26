package io.resys.crm.client.spi.visitors;

import java.util.ArrayList;
import java.util.Arrays;
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
import io.resys.crm.client.api.model.CustomerCommand.CustomerCommandType;
import io.resys.crm.client.api.model.CustomerCommand.CustomerUpdateCommand;
import io.resys.crm.client.api.model.Document;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.crm.client.spi.store.DocumentStore;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.crm.client.spi.store.MainBranch;
import io.resys.crm.client.spi.visitors.CustomerCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.entities.Tenant.CommitResultStatus;
import io.resys.thena.api.entities.doc.ThenaDocObject.Doc;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocBranch;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocCommit;
import io.resys.thena.api.entities.doc.ThenaDocObject.DocLog;
import io.resys.thena.api.entities.doc.ThenaDocObjects.DocObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateCustomerVisitor implements DocObjectsVisitor<Uni<List<Customer>>> {
  private final DocumentStore ctx;
  private final List<String> customerIds;
  private final ModifyManyDocBranches updateBuilder;
  private final CreateManyDocs createBuilder;
  private final Map<String, List<CustomerUpdateCommand>> commandsByCustomerId; 
  private final List<CustomerCommandType> upserts = Arrays.asList(CustomerCommandType.UpsertSuomiFiPerson, CustomerCommandType.UpsertSuomiFiPerson);
  
  public UpdateCustomerVisitor(List<CustomerUpdateCommand> commands, DocumentStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByCustomerId = commands.stream()
        .collect(Collectors.groupingBy(CustomerUpdateCommand::getCustomerId));
    this.customerIds = new ArrayList<>(commandsByCustomerId.keySet());
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .message("Update customers: " + commandsByCustomerId.size())
        .author(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createManyDocs()
        .docType(Document.DocumentType.CUSTOMER.name())
        .message("Upsert customers: " + commandsByCustomerId.size())
        .author(config.getAuthor().get())
        .branchName(config.getBranchName());
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
    
    final var totalUpserts = this.commandsByCustomerId.values().stream().flatMap(e -> e.stream()).filter(e -> upserts.contains(e.getCommandType())).count();
    if(customerIds.size() < Math.max((result.getDocs().size() - totalUpserts), 0)) {
      throw new DocumentStoreException("CUSTOMERS_UPDATE_FAIL_NOT_ALL_CUSTOMERS_FOUND", JsonObject.of("failedUpdates", customerIds));
    }
    return result;
  }

  @Override
  public Uni<List<Customer>> end(DocumentConfig config, DocObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<Customer>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<Customer>> applyInserts(DocumentConfig config, DocObjects blob) {
    final var insertedCustomers = new ArrayList<Customer>(); 
    for(final var entry : commandsByCustomerId.entrySet()) {
      try {
        if(!upserts.contains(entry.getValue().get(0).getCommandType())) {
          continue;
        }
        final var inserted = new CustomerCommandVisitor(ctx.getConfig()).visitTransaction(entry.getValue());
        this.createBuilder.item()
          .docId(inserted.getId())
          .externalId(inserted.getExternalId())
          .append(JsonObject.mapFrom(inserted))
          .next();
        insertedCustomers.add(inserted);
      } catch(NoChangesException e) {
        // nothing to do
      }
    }
    
    if(insertedCustomers.isEmpty()) {
      return Uni.createFrom().item(insertedCustomers);
    }
    return createBuilder.build().onItem().transform(envelope -> mapInsertedResponse(envelope, insertedCustomers));
  }

  private Uni<List<Customer>> applyUpdates(DocumentConfig config, DocObjects blob) {
    final var updatedCustomers = blob.accept((Doc doc, DocBranch docBranch, DocCommit commit, List<DocLog> log) -> {  
      final var start = docBranch.getValue().mapTo(ImmutableCustomer.class);
      
      final List<CustomerUpdateCommand> commands = new ArrayList<>();
      if(commandsByCustomerId.containsKey(start.getId())) {
        commands.addAll(commandsByCustomerId.get(start.getId()));
        commandsByCustomerId.remove(start.getId());
      }
      if(commandsByCustomerId.containsKey(start.getExternalId())) {
        commands.addAll(commandsByCustomerId.get(start.getExternalId()));
        commandsByCustomerId.remove(start.getExternalId());
      }
      
      if(commands.isEmpty()) {
        throw DocumentStoreException.builder("CUSTOMERS_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new CustomerCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
        this.updateBuilder.item()
          .docId(updated.getId())
          .branchName(docBranch.getBranchName())
          .append(JsonObject.mapFrom(updated))
          .next();
        
        return updated;
      } catch(NoChangesException e) {
        return start;
      }
    });
    
    if(updatedCustomers.isEmpty()) {
      return Uni.createFrom().item(updatedCustomers);
    }
    
    return updateBuilder.build().onItem().transform(response -> mapUpdateResponse(response, updatedCustomers));
  }
  
  
  private List<Customer> mapInsertedResponse(ManyDocsEnvelope envelope, List<Customer> insertedCustomers) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw new DocumentStoreException("CUSTOMER_CREATE_FAIL", DocumentStoreException.convertMessages(envelope));
    }
    
    final var branches = envelope.getBranch();
    final Map<String, Customer> createdById = new HashMap<>(insertedCustomers.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    branches.forEach(branch -> {
      final var next = ImmutableCustomer.builder()
          .from(createdById.get(branch.getDocId()))
          .version(branch.getCommitId())
          .build();
      
      createdById.put(next.getId(), next);
    });
    return Collections.unmodifiableList(new ArrayList<>(createdById.values()));
  }
  
  private List<Customer> mapUpdateResponse(ManyDocsEnvelope response, List<Customer> updatedCustomers) {
    if(response.getStatus() != CommitResultStatus.OK) {
      final var failedUpdates = customerIds.stream().collect(Collectors.joining(",", "{", "}"));
      throw new DocumentStoreException("CUSTOMERS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocumentStoreException.convertMessages(response));
    }
    final Map<String, Customer> customerById = new HashMap<>(updatedCustomers.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getCommit().forEach(commit -> {
      
      final var next = ImmutableCustomer.builder()
          .from(customerById.get(commit.getDocId()))
          .version(commit.getId())
          .build();
      customerById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(customerById.values()));
  }
}
