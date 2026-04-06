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

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramStatus;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeploymentEnvirValidator {
  private final EveliDeployment deployment;
  private final Sites sites;
  private final ProgramEnvir programs;
  private final EveliDeploymentCompilerLogger logger;
  private int errorIndex;
  
  public int accept() {
    visitSites(sites);
    return errorIndex;
  }

  private void visitSites(Sites sites) {
    sites.getSites().forEach(this::visitSite);
    if(sites.getSites().isEmpty()) {
      logger.stencilMissing();
      addError();
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
      logger.formNameMissing(locale, link);
      addError();
      return;
    }
    if(link.getFormTag() == null) {
      logger.formTagMissing(locale, link);
      addError();
      return;
    }
    if(link.getFormId() == null) {
      logger.formIdMissing(locale, link);
      addError();
      return;
    }
    
    final var form = deployment.getSources().getDialob().stream()
        .filter(e -> link.getFormId().equals(e.getId()))
        .findFirst();
    if(form.isEmpty()) {
      logger.formIdMissing(locale, link);
      addError();
      return;
    }
    
    if(!form.get().getMetadata().getLanguages().contains(locale)) {
      logger.formLocaleMissing(form.get(), locale, link);
      addError();
      return;
    }
    
    logger.topicLinkFormOk(form.get(), locale, link);
  }
  
  private void visitFlow(TopicLink link) {
    final var flowName = link.getFlowName();
    if(flowName == null) {
      logger.flowNameMissing(link);
      addError();
      return;
    }
    
    if(!programs.getFlowsByName().containsKey(flowName)) {
      logger.flowMissing(link);
      addError();
      return;
    }
    
    final ProgramWrapper<AstFlow, FlowProgram> flow = programs.getFlowsByName().get(flowName);
    if(flow.getStatus() != ProgramStatus.UP) {
      logger.flowBroken(link, flow);
      addError();
      return;
    }
    
    logger.topicLinkFlowOk(flow, link);
  }
  
  private void addError() {
    errorIndex++;
  }
}
