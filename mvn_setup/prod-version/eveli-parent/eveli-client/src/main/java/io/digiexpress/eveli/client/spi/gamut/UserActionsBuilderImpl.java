package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.time.Duration;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.rest.IdAndRevision;
import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.GamutClient.UserActionBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessCommands.ProcessStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionsBuilderImpl implements UserActionBuilder {
  
  private final ProcessRepository processRepository;
  
  private final DialobCommands dialobCommands;
  private final HdesCommands hdesCommands;
  private final EveliAssetClient assetClient;
  
  
  private final CrmClient auth;
  
  private String actionId;
  private String clientLocale; 
  private String inputContextId;
  private String inputParentContextId;
  
  public UserAction createOne() throws UserActionNotAllowedException, WorkflowNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(clientLocale, () -> "clientLocale can't be null!");
    TaskAssert.notNull(inputContextId, () -> "inputContextId can't be null!");
    TaskAssert.notNull(inputParentContextId, () -> "inputParentContextId can't be null!");    
    
    if(auth.getCustomer().getPrincipal().getRepresentedId() != null) {
      final var userRoles = auth.getCustomerRoles().getRoles();  
      final var allowed = hdesCommands.processAuthorizationQuery().get(ImmutableInitProcessAuthorization.builder()
          .addAllUserRoles(userRoles)
          .build());
      
       if(!allowed.getAllowedProcessNames().contains(actionId)) {
         throw new UserActionNotAllowedException("Process: " + actionId + " blocked, allowed list: "  + allowed.getAllowedProcessNames() + "!");
       }
    }
    
    final var request = visitRequest();
    
    final Workflow workflow = assetClient.queryBuilder().findOneWorkflowByName(clientLocale)
        .await().atMost(Duration.ofMinutes(1))
        .map(e -> e.getBody())
        .orElseThrow(() -> new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find workflow by name: '").append(clientLocale).append("'!")
        .toString()));

    final var sessionId = visitForm(request, workflow).getId();    
    
    final ProcessEntity process = processRepository.save(new ProcessEntity()
        .setQuestionnaire(sessionId)
        .setUserId(request.getIdentity())
        .setStatus(ProcessStatus.CREATED)
        .setWorkflowName(request.getWorkflowName())
        .setInputContextId(request.getInputContextId())
        .setInputParentContextId(request.getInputParentContextId()));

    return ImmutableUserAction.builder()
        .id(process.getId().toString())
        .status(process.getStatus().name())
        .created(process.getCreated())
        .updated(process.getUpdated())
        .name(process.getWorkflowName())
        .inputContextId(process.getInputContextId())
        .inputParentContextId(process.getInputParentContextId())
        .formId(process.getQuestionnaire())
        .formInProgress(true)
        .viewed(true)
        
        // deprecated
        .messagesUri("not-needed")
        .reviewUri("not-needed")
        .formUri("not-needed")
        .build();
  }

  
  private IdAndRevision visitForm(InitUserAction request, Workflow workflow) {
    final var formBuilder = dialobCommands.create()
        .formName(workflow.getFormName())
        .formTag(workflow.getFormTag())
        .language(clientLocale)
        .addContext("FirstNames", request.getFirstName())
        .addContext("LastName", request.getLastName())
        .addContext("SocialSecurityNumber", request.getIdentity()) // same field is used for company id and ssn
        .addContext("Email", request.getEmail())
        .addContext("Address", request.getAddress())
        .addContext("ProtectionOrder", request.getProtectionOrder());
        
      if(request.getCompanyName() != null) {
        formBuilder
          .addContext("CompanyName", request.getCompanyName())
          .addContext("CompanyId", request.getIdentity());  // same field is used for company id and ssn
      }
      
      if(request.getRepresentativeIdentity() != null) {
        formBuilder
        .addContext("RepresentativeEnabled", true)
        .addContext("RepresentativeFirstName", request.getRepresentativeFirstName())
        .addContext("RepresentativeLastName", request.getRepresentativeLastName())
        .addContext("RepresentativeIdentity", request.getRepresentativeIdentity());
      } else {
        formBuilder.addContext("RepresentativeEnabled", false);
      }
      if (request.getInputContextId() != null) {
        formBuilder.addContext("inputContextId", request.getInputContextId());
      }
      if (request.getInputParentContextId() != null) {
        formBuilder.addContext("inputParentContextId", request.getInputParentContextId());
      }
      
    return formBuilder.build();
  }

  private InitUserAction visitRequest() {
    final var user = auth.getCustomer().getPrincipal();
    final var person = user.getRepresentedPerson();
    final var company = user.getRepresentedCompany();
    
    final var init = ImmutableInitUserAction.builder()
        .inputContextId(inputContextId)
        .inputParentContextId(inputParentContextId)
        .workflowName(actionId)
        .protectionOrder(user.getProtectionOrder())
        .language(clientLocale);
    
    if(person != null) {
      final var representativeName = person.getRepresentativeName();
      final var representativeFirstName = representativeName[1];  
      final var representativeLastName = representativeName[0];
      return init      
        .firstName(representativeFirstName)
        .lastName(representativeLastName)
        .identity(person.getPersonId())
        .representativeFirstName(user.getFirstName())
        .representativeLastName(user.getLastName())
        .representativeIdentity(user.getSsn())
        .build();
      
      
    } else if(company != null) {
      return init
        .companyName(company.getName())
        .lastName(company.getName())
        .identity(company.getCompanyId())
        .representativeFirstName(user.getFirstName())
        .representativeLastName(user.getLastName())
        .representativeIdentity(user.getSsn())
        .build();
    } else {
      return init
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .identity(user.getSsn())
        .email(user.getContact().getEmail())
        .address(user.getContact().getAddressValue())
        .build();
    }
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableInitUserAction.class)
  @JsonDeserialize(as = ImmutableInitUserAction.class)
  interface InitUserAction {
    String getIdentity();
    String getWorkflowName();
    Boolean getProtectionOrder();    

    @Nullable
    String getCompanyName();
    @Nullable
    String getFirstName();
    @Nullable
    String getLastName();
    @Nullable
    String getLanguage();
    @Nullable
    String getEmail();
    @Nullable
    String getAddress();

    @Nullable
    String getRepresentativeFirstName();
    @Nullable
    String getRepresentativeLastName();
    @Nullable
    String getRepresentativeIdentity();
    @Nullable
    String getInputContextId();
    @Nullable
    String getInputParentContextId();
  }
}
