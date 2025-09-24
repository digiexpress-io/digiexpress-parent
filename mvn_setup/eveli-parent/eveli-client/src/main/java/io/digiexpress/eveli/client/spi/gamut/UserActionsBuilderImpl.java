package io.digiexpress.eveli.client.spi.gamut;

/*-
 * #%L
 * eveli-client
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
import java.time.ZoneId;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.rest.IdAndRevision;
import io.digiexpress.eveli.client.api.GamutAuthClient.Customer;
import io.digiexpress.eveli.client.api.GamutAuthClient.CustomerRoles;
import io.digiexpress.eveli.client.api.GamutClient.DialobFormNotFoundException;
import io.digiexpress.eveli.client.api.GamutClient.UserAction;
import io.digiexpress.eveli.client.api.GamutClient.UserActionBuilder;
import io.digiexpress.eveli.client.api.GamutClient.UserActionMeta;
import io.digiexpress.eveli.client.api.GamutClient.UserActionNotAllowedException;
import io.digiexpress.eveli.client.api.GamutClient.WorkflowNotFoundException;
import io.digiexpress.eveli.client.api.ImmutableInitProcessAuthorization;
import io.digiexpress.eveli.client.api.ImmutableUserAction;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessType;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class UserActionsBuilderImpl implements UserActionBuilder {
  private final ProcessClient hdesCommands;  
  private final DialobClient dialobCommands;
  private final EveliEnvirClient envir;
  private boolean anon = false;
  private boolean customerAssignment = false;
  private Optional<Customer> customer = Optional.empty();
  private Optional<CustomerRoles> customerRoles = Optional.empty();
  private Optional<InitUserAction> externalUserActionInit = Optional.empty();
  
  private String actionId;
  private String taskId;
  private String clientLocale; 
  private String inputContextId;
  private String inputParentContextId;
  private final UserActionLogger userActionLogger = new UserActionLogger();
  
  public UserActionBuilder externalUserActionInit(InitUserAction customer) {
    this.externalUserActionInit = Optional.ofNullable(customer);
    return this;
  }
  public UserActionBuilder customer(Customer customer) {
    this.customer = Optional.ofNullable(customer);
    return this;
  }
  public UserActionBuilder customerRoles(CustomerRoles customerRoles) {
    this.customerRoles = Optional.ofNullable(customerRoles);
    return this;
  }
  public Uni<UserAction> createOne() throws UserActionNotAllowedException, WorkflowNotFoundException {
    TaskAssert.notNull(actionId, () -> "actionId can't be null!");
    TaskAssert.notNull(clientLocale, () -> "clientLocale can't be null!");
    TaskAssert.notNull(inputContextId, () -> "inputContextId can't be null!");
    TaskAssert.notNull(inputParentContextId, () -> "inputParentContextId can't be null!");
    
    
    userActionLogger.startRuntime();
    return envir.runtimeQuery().getOne()
        .invoke(runtime -> userActionLogger.endRuntime(runtime))
        .onItem().transformToUni(runtime -> createUserAction(runtime))
        .onItem().invoke(action -> userActionLogger.close());
  }
  
  private Uni<UserAction> createUserAction(EveliRuntime runtime) {
    
    userActionLogger.startStencilService();
    final UserActionMeta meta = new UserActionMetaQueryImpl(envir).actionId(actionId).locale(clientLocale).getOne(runtime);
    userActionLogger.endStencilService(meta);
    
    final var now = OffsetDateTime.now();
    final var sites = runtime.getStencil(now);
    final var stencilService = meta.getTopicLink();
    final var expiresInSeconds = meta.getExpiresInSeconds();
    
    
    if(customer.isPresent()) {
      userActionLogger.startAuth();
      if(customer.get().getPrincipal().getRepresentedId() != null) {
        final var userRoles = customerRoles.get().getRoles();
        userActionLogger.endAuth();
        
        userActionLogger.startWrenchAllowedRoles();
        final var allowed = hdesCommands.queryAuthorization().get(ImmutableInitProcessAuthorization.builder()
            .addAllUserRoles(userRoles)
            .build()).getAllowedProcessNames();
        userActionLogger.endWrenchAllowedRoles();
        
         if(!(
             allowed.contains(stencilService.getValue()) ||
             allowed.contains(actionId) ||
             allowed.contains(stencilService.getName())
          )) {
           throw new UserActionNotAllowedException("Process: " + actionId + " blocked, allowed list: "  + allowed + "!");         
         }
      } else {
        userActionLogger.endAuth();
      }
    }

    
    if(sites.getSites().get(clientLocale) == null) {
      throw new WorkflowNotFoundException(new StringBuilder()
          .append("Can't find stencil service for locale: '").append(clientLocale).append("'!")
          .toString());
    }
    
    final var request = customer.isPresent() ? visitRequest(customer.get()) : externalUserActionInit.orElseThrow(() -> 
      new UserActionNotAllowedException("Process: " + actionId + " blocked, there is no customer data, 'Customer' or 'InitUserAction'!")     
    );
    
    
    return visitForm(request, stencilService).onItem().transform(revision -> {
      
      final var sessionId = revision.getId();
      userActionLogger.startProcessInstance();
      final var process = hdesCommands.createInstance()
          .questionnaireId(sessionId)
          .userId(request.getIdentity())
          .expiresInSeconds(expiresInSeconds)
          .expiresAt(stencilService.getEndDate() != null ? stencilService.getEndDate().atZone(ZoneId.systemDefault()).toOffsetDateTime() : null)
          .anon(anon)
          .taskId(taskId)
          
          .workflowName(stencilService.getValue())
          .articleName(request.getInputContextId())
          .parentArticleName(request.getInputParentContextId())
          .flowName(stencilService.getFlowName())
          .formName(stencilService.getFormName())
          
          .formTagName(stencilService.getFormTag())
          .stencilTagName(runtime.getStencilTagName())
          .wrenchTagName(runtime.getWrenchTagName())
          .customerAssignment(customerAssignment)
          
          .create();
      
      userActionLogger.endProcessInstance();

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
          .assigned(process.getType() == ProcessType.CUSTOMER_ASSIGNMENT ? true : false)
          .viewed(true)
          .taskId(taskId)
          
          // deprecated
          .messagesUri("not-needed")
          .reviewUri("not-needed")
          .formUri("not-needed")
          .build();
    });
  }

  private Uni<String> getFormId(final TopicLink stencilService) {
    return Uni.combine().all().unis(
        getFormIdById(stencilService),
        getFormIdByTag(stencilService)
    ).asTuple().onItem().transform(tuple -> {
      final var formId = Optional.ofNullable(tuple.getItem1()).orElse(tuple.getItem2());
      return formId;
    });
  }
  

  private Uni<String> getFormIdById(final TopicLink stencilService) {
    return Uni.createFrom().item(() -> {
      try {
        if(StringUtils.isAllBlank(stencilService.getFormId())) {
          return null;
        }
        userActionLogger.startFormId();
        final var form = dialobCommands.getFormById(stencilService.getFormId());
        return form.getId();  
      } catch(Exception e) {
        // not the end of the world
        log.info("Can't resolve for by tag or form name, will try by form id for topic: {}", JsonObject.mapFrom(stencilService).encodePrettily());
        return null;
      } finally {
        userActionLogger.endFormId(); 
      }
      
    });
  }
  

  private Uni<String> getFormIdByTag(final TopicLink stencilService) {
    return Uni.createFrom().item(() -> {
      try {

        final var formName = stencilService.getFormName();
        final var formTagName = stencilService.getFormTag();
        userActionLogger.startFormTag();
        final var formTag = dialobCommands.getFormTag(formName, formTagName);
        return formTag.getFormId();
      } catch(Exception e) {
        // not the end of the world
        log.info("Can't resolve for by tag or form name, will try by form id for topic: {}", JsonObject.mapFrom(stencilService).encodePrettily());
        return null;
      } finally {
        userActionLogger.endFormTag();
      }
    });
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
  
  private Uni<IdAndRevision> visitForm(InitUserAction request, TopicLink stencilService) {
    return getFormId(stencilService).onItem().transform(formId -> {
      
      if(formId == null) {
        throw new DialobFormNotFoundException("Can't find dialob form connected to stencil service: " + JsonObject.mapFrom(stencilService).encodePrettily());
      }
      
      
      final var formBuilder = dialobCommands.createSession()
          .formId(formId)
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
        
      userActionLogger.startFormCreate();
      final var result = formBuilder.build();
      userActionLogger.endFormCreate();
      
      return result;
      
    });
    
    

  }

  private InitUserAction visitRequest(Customer customer) {
    final var user = customer.getPrincipal();
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
  public interface InitUserAction {
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
