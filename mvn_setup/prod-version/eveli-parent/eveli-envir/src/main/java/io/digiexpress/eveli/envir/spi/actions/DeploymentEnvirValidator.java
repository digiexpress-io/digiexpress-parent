package io.digiexpress.eveli.envir.spi.actions;

/*-
 * #%L
 * eveli-envir
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

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeploymentEnvirValidator {

  private final EveliDeployment deployment;
  private final Sites sites;
  private final ProgramEnvir programs;
  private final JsonObject errors = JsonObject.of();
  private int errorIndex;
  
  public Optional<JsonObject> accept() {
    visitSites(sites);
    
    if(errors.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(errors);
  }

  private void visitSites(Sites sites) {
    sites.getSites().forEach(this::visitSite);
    if(sites.getSites().isEmpty()) {
      addError("STENCIL_CONTENT_MISSING", "stencil content is empty nothing to deploy!");
    }
  }
  
  private void visitSite(String locale, LocalizedSite site) {
    site.getLinks().values().stream().forEach(link -> visitTopicLink(locale, link)); 
  }
  
  private void visitTopicLink(String locale, TopicLink link) {
    if(!Boolean.TRUE.equals(link.getWorkflow())) {
      return;
    }
    visitFlow(link);
    visitForm(locale, link);
  }
  
  private void visitForm(String locale, TopicLink link) {
    if(link.getFormName() == null) {
      addError("STENCIL_WORKFLOW_FORM_NAME_MISSING", "form name must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    if(link.getFormTag() == null) {
      addError("STENCIL_WORKFLOW_FORM_TAG_MISSING", "form tag must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    if(link.getFormId() == null) {
      addError("STENCIL_WORKFLOW_FORM_ID_MISSING", "form id must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    
    
    final var form = deployment.getSources().getDialob().stream()
        .filter(e -> link.getFormId().equals(e.getId()))
        .findFirst();
    if(form.isEmpty()) {
      addError("STENCIL_WORKFLOW_FORM_MISSING", "form id must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    
    if(!form.get().getMetadata().getLanguages().contains(locale)) {
      addError("STENCIL_WORKFLOW_FORM_LOCALE_MISSING", "form locale must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    
  }
  
  private void visitFlow(TopicLink link) {
    final var flowName = link.getFlowName();
    if(flowName == null) {
      addError("STENCIL_WORKFLOW_WRENCH_FLOW_NAME_MUST_BE_DEFINED", "flow name must be defined for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    
    if(!programs.getFlowsByName().containsKey(flowName)) {
      addError("WRENCH_FLOW_MISSING", "wrench flow can't be found for workflow: " + JsonObject.mapFrom(link).encodePrettily());
      return;
    }
    
    final ProgramWrapper<AstFlow, FlowProgram> flow = programs.getFlowsByName().get(flowName);
    if(flow.getStatus() != ProgramStatus.UP) {
      addError("WRENCH_FLOW_BROKEN", "wrench flow in status: " + flow.getStatus() + " with errors: " + JsonObject.mapFrom(flow.getErrors()).encodePrettily());
      return;
    }
  }
  
  private void addError(String code, String error) {
    errorIndex++;
    errors.put(errorIndex + "-" + code, error);
  }
}
