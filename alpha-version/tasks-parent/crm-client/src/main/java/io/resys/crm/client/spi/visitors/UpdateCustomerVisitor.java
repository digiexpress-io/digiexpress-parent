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
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.CrmStore;
import io.resys.crm.client.spi.visitors.CustomerCommandVisitor.NoChangesException;
import io.resys.thena.api.actions.DocCommitActions.CreateManyDocs;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.api.actions.DocQueryActions.IncludeInQuery;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.api.entities.doc.DocCommands;
import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocCommitTree;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.DocObjectsVisitor;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class UpdateCustomerVisitor implements DocObjectsVisitor<Uni<List<Customer>>> {
  private final CrmStore ctx;
  private final List<String> customerIds;
  private final ModifyManyDocBranches updateBuilder;
  private final CreateManyDocs createBuilder;
  private final Map<String, List<CustomerUpdateCommand>> commandsByCustomerId; 
  private final List<CustomerCommandType> upserts = Arrays.asList(CustomerCommandType.UpsertSuomiFiPerson, CustomerCommandType.UpsertSuomiFiPerson);
  
  public UpdateCustomerVisitor(List<CustomerUpdateCommand> commands, CrmStore ctx) {
    super();
    this.ctx = ctx;
    final var config = ctx.getConfig();
    this.commandsByCustomerId = commands.stream()
        .collect(Collectors.groupingBy(CustomerUpdateCommand::getCustomerId));
    this.customerIds = new ArrayList<>(commandsByCustomerId.keySet());
    this.updateBuilder = config.getClient().doc(config.getRepoId()).commit().modifyManyBranches()
        .commitMessage("Update customers: " + commandsByCustomerId.size())
        .commitAuthor(config.getAuthor().get());
    this.createBuilder = config.getClient().doc(config.getRepoId()).commit().createManyDocs()
        .commitMessage("Upsert customers: " + commandsByCustomerId.size())
        .commitAuthor(config.getAuthor().get());
  }

  @Override
  public Uni<QueryEnvelope<DocTenantObjects>> start(ThenaDocConfig config, DocObjectsQuery builder) {
    return builder.include(IncludeInQuery.COMMANDS).findAll(customerIds);
  }

  @Override
  public DocTenantObjects visitEnvelope(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("GET_CUSTOMERS_BY_IDS_FOR_UPDATE_FAIL")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    final var result = envelope.getObjects();
    if(result == null) {
      throw DocStoreException.builder("GET_CUSTOMERS_BY_IDS_FOR_UPDATE_NOT_FOUND")   
        .add(config, envelope)
        .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
        .build();
    }
    
    final var totalUpserts = this.commandsByCustomerId.values().stream().flatMap(e -> e.stream()).filter(e -> upserts.contains(e.getCommandType())).count();
    if(customerIds.size() < Math.max((result.getDocs().size() - totalUpserts), 0)) {
      throw new DocStoreException("CUSTOMERS_UPDATE_FAIL_NOT_ALL_CUSTOMERS_FOUND", JsonObject.of("failedUpdates", customerIds));
    }
    return result;
  }

  @Override
  public Uni<List<Customer>> end(ThenaDocConfig config, DocTenantObjects blob) {
    return applyUpdates(config, blob).onItem()
      .transformToUni(updated -> applyInserts(config, blob).onItem().transform(inserted -> {
        final var result = new ArrayList<Customer>();
        result.addAll(updated);
        result.addAll(inserted);
        return Collections.unmodifiableList(result);
      }));
  }
  
  private Uni<List<Customer>> applyInserts(ThenaDocConfig config, DocTenantObjects blob) {
    final var insertedCustomers = new ArrayList<Customer>(); 
    for(final var entry : commandsByCustomerId.entrySet()) {
      try {
        if(!upserts.contains(entry.getValue().get(0).getCommandType())) {
          continue;
        }
        final var inserted = new CustomerCommandVisitor(ctx.getConfig()).visitTransaction(entry.getValue());
        this.createBuilder.item()
          .docType(CrmStore.DOC_TYPE_CUSTOMER)
          .docId(inserted.getItem1().getId())
          .externalId(inserted.getItem1().getExternalId())
          .branchContent(JsonObject.mapFrom(inserted.getItem1()))
          .commands(inserted.getItem2())
          .next();
        insertedCustomers.add(inserted.getItem1());
      } catch(NoChangesException e) {
        // nothing to do
      }
    }
    
    if(insertedCustomers.isEmpty()) {
      return Uni.createFrom().item(insertedCustomers);
    }
    return createBuilder.build().onItem().transform(envelope -> mapInsertedResponse(envelope, insertedCustomers));
  }

  private Uni<List<Customer>> applyUpdates(ThenaDocConfig config, DocTenantObjects blob) {
    final var updatedCustomers = blob.accept((Doc doc, 
        DocBranch docBranch, 
        Map<String, DocCommit> commit, 
        List<DocCommands> _commands,
        List<DocCommitTree> trees) -> {  
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
        throw DocStoreException.builder("CUSTOMERS_UPDATE_FAIL_COMMANDS_ARE_EMPTY")   
          .add((callback) -> callback.addArgs(customerIds.stream().collect(Collectors.joining(",", "{", "}"))))
          .build();
      }
      try {
        final var updated = new CustomerCommandVisitor(start, ctx.getConfig()).visitTransaction(commands);
        this.updateBuilder.item()
          .docId(updated.getItem1().getId())
          .replace(JsonObject.mapFrom(updated.getItem1()))
          .commands(updated.getItem2())
          .next();
        
        return updated.getItem1();
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
      throw new DocStoreException("CUSTOMER_CREATE_FAIL", DocStoreException.convertMessages(envelope));
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
      throw new DocStoreException("CUSTOMERS_UPDATE_FAIL", JsonObject.of("failedUpdates", failedUpdates), DocStoreException.convertMessages(response));
    }
    final Map<String, Customer> customerById = new HashMap<>(updatedCustomers.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)));
    response.getBranch().forEach(commit -> {
      
      final var next = FindAllCustomersVisitor.mapToCustomer(commit, response.getCommands().stream()
          .filter(command -> command.getDocId().equals(commit.getDocId()))
          .toList());
      customerById.put(next.getId(), next);
    });
    
    return Collections.unmodifiableList(new ArrayList<>(customerById.values()));
  }
}
