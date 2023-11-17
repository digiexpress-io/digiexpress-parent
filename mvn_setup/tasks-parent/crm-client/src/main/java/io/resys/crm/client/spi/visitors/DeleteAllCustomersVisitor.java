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

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.crm.client.api.model.Customer;
import io.resys.crm.client.api.model.Document;
import io.resys.crm.client.api.model.ImmutableCustomer;
import io.resys.crm.client.spi.store.DocumentConfig;
import io.resys.crm.client.spi.store.DocumentConfig.DocObjectsVisitor;
import io.resys.crm.client.spi.store.DocumentStoreException;
import io.resys.thena.docdb.api.actions.CommitActions.CommitResultStatus;
import io.resys.thena.docdb.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocBranches;
import io.resys.thena.docdb.api.actions.DocCommitActions.ModifyManyDocs;
import io.resys.thena.docdb.api.actions.DocQueryActions.DocObjectsQuery;
import io.resys.thena.docdb.api.models.QueryEnvelope;
import io.resys.thena.docdb.api.models.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.docdb.api.models.ThenaDocObjects.DocObjects;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteAllCustomersVisitor implements DocObjectsVisitor<Uni<List<Customer>>>{

  private final String userId;
  private final Instant targetDate;
  
  private ModifyManyDocBranches archiveCommand;
  private ModifyManyDocs removeCommand;
  
  @Override
  public DocObjectsQuery start(DocumentConfig config, DocObjectsQuery query) {
    this.removeCommand = config.getClient().doc().commit().modifyManyDocs()
        .repoId(config.getRepoId())
        .author(config.getAuthor().get())
        .message("Delete Tenants");
    
    // Build the blob criteria for finding all documents of type Project
    return query.docType(Document.DocumentType.CRM.name());
  }

  @Override
  public DocObjects visitEnvelope(DocumentConfig config, QueryEnvelope<DocObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocumentStoreException.builder("FIND_ALL_TENANTS_FAIL_FOR_DELETE").add(config, envelope).build();
    }
    return envelope.getObjects();
  }
  
  @Override
  public Uni<List<Customer>> end(DocumentConfig config, DocObjects ref) {
    if(ref == null) {
      return Uni.createFrom().item(Collections.emptyList());
    }

    final var tenantsRemoved = visitTree(ref);    
    return archiveCommand.build()
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("TENANT_ARCHIVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transformToUni(archived -> removeCommand.build())
      .onItem().transform((ManyDocsEnvelope commit) -> {
        if(commit.getStatus() == CommitResultStatus.OK) {
          return commit;
        }
        throw new DocumentStoreException("TENANT_REMOVE_FAIL", DocumentStoreException.convertMessages(commit));
      })
      .onItem().transform((commit) -> tenantsRemoved);
  }

  
  
  
  private List<Customer> visitTree(DocObjects state) {
    return state.getBranches().values().stream().flatMap(e -> e.stream())
      .map(blob -> blob.getValue().mapTo(ImmutableCustomer.class))
      .map(TenantConfig -> visitTenantConfig(TenantConfig))
      .collect(Collectors.toUnmodifiableList());
  }
  private Customer visitTenantConfig(Customer customer) {
    
    final var json = JsonObject.mapFrom(customer);

    removeCommand.item().docId(customer.getId()).remove();
    return customer;
  }

}
