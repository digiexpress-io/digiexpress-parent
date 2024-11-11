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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.rest.IdAndRevision;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.client.api.CrmClient;
import io.digiexpress.eveli.client.api.GamutClient.UserActionBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.UserActionsClient.UserAction;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionsBuilderImpl implements UserActionBuilder {
  private final ProcessClient hdesCommands;  
  private final DialobClient dialobCommands;
  private final Supplier<Sites> siteEnvir;
  private final Supplier<ProgramEnvir> programEnvir;
  private final Supplier<WorkflowTag> workflowEnvir;
  
  
  
  private final CrmClient auth;
  private final ZoneOffset offset;
  
  
  private String actionId;
  private String clientLocale; 
  private String inputContextId;
  private String inputParentContextId;
  
  
  public Uni<UserAction> createOne() throws UserActionNotAllowedException, WorkflowNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(clientLocale, () -> "clientLocale can't be null!");
    TaskAssert.notNull(inputContextId, () -> "inputContextId can't be null!");
    TaskAssert.notNull(inputParentContextId, () -> "inputParentContextId can't be null!");    
    
    return Uni.createFrom().item(siteEnvir.get())
        .onItem()
        .transform(site -> createUserAction(site));
  }
  
  private UserAction createUserAction(Sites site) {
    if(auth.getCustomer().getPrincipal().getRepresentedId() != null) {
      final var userRoles = auth.getCustomerRoles().getRoles();  
      final var allowed = hdesCommands.queryAuthorization().get(ImmutableInitProcessAuthorization.builder()
          .addAllUserRoles(userRoles)
          .build());
      
       if(!allowed.getAllowedProcessNames().contains(actionId)) {
         throw new UserActionNotAllowedException("Process: " + actionId + " blocked, allowed list: "  + allowed.getAllowedProcessNames() + "!");
       }
    }
    
    if(siteEnvir.get().getSites().get(clientLocale) == null) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service for locale: '").append(clientLocale).append("'!")
          .toString());
    }
    
    
    final var request = visitRequest();
    final var stencilSite = siteEnvir.get();
    final var stencilService = stencilSite.getSites().get(clientLocale).getLinks().get(actionId);
    
    
    if(stencilService == null) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }

    final var expiresInSeconds = stencilService.getEndDate() == null ? null : ChronoUnit.SECONDS.between(Instant.now().atOffset(offset).toLocalDateTime(), stencilService.getEndDate());
    if(expiresInSeconds != null && expiresInSeconds <= 0) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service by id: '").append(actionId).append("'!")
          .toString());
    }
    
    final var wkEnvir = workflowEnvir.get();
    final Workflow workflow = wkEnvir.getEntries().stream()
        .filter(w -> w.getName().equals(stencilService.getValue()))
        .findFirst()
        .orElseThrow(() -> new WorkflowNotFoundException(new StringBuilder()
        .append("Can't find workflow by name: '").append(clientLocale).append("'!")
        .toString()));
    
    final var sessionId = visitForm(request, workflow).getId();
    
    final var process = hdesCommands.createInstance()
        .questionnaireId(sessionId)
        .userId(request.getIdentity())
        .expiresInSeconds(expiresInSeconds)
        .expiresAt(stencilService.getEndDate())
        
        .workflowName(workflow.getName())
        .articleName(request.getInputContextId())
        .parentArticleName(request.getInputParentContextId())
        .flowName(workflow.getFlowName())
        .formName(workflow.getFormName())
        
        .formTagName(workflow.getFormTag())
        .stencilTagName(stencilSite.getTagName())
        .wrenchTagName(programEnvir.get().getTagName())
        .workflowTagName(wkEnvir.getName())
        
        
        .create();

    return ImmutableUserAction.builder()
        .id(process.getId().toString())
        .status(process.getStatus().name())
        .created(process.getCreated())
        .updated(process.getUpdated())
        .name(process.getWorkflowName())
        .inputContextId(visitArticleName(process.getArticleName()))
        .inputParentContextId(process.getParentArticleName())
        .formId(process.getQuestionnaireId())
        .formInProgress(true)
        .viewed(true)
        
        // deprecated
        .messagesUri("not-needed")
        .reviewUri("not-needed")
        .formUri("not-needed")
        .build();
  }

  private String visitArticleName(String articleName) {
    if(StringUtils.isEmpty(articleName)) {
      return null;
    }
    if(articleName.charAt(3) == '_') {
      return articleName.substring(4);      
    }
    // no ordering
    return articleName;
  }
  
  private IdAndRevision visitForm(InitUserAction request, Workflow workflow) {
    final var formBuilder = dialobCommands.createSession()
        .formId(workflow.getFormId())
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
        .inputContextId(visitArticleName(inputContextId))
        .inputParentContextId(visitArticleName(inputParentContextId))
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
