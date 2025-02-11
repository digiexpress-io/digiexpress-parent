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

import io.dialob.api.form.Form;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeploymentEnvirDialobUploader {
  private final DialobClient dialobClient;
  private final EveliDeployment deployment;
  private final Sites sites;
  private final EveliDeploymentCompilerLogger logger;
  private int errorIndex;
  
  public int accept() {
    visitSites(sites);
    return errorIndex;
  }

  private void visitSites(Sites sites) {
    sites.getSites().forEach(this::visitSite);
  }
  
  private void visitSite(String locale, LocalizedSite site) {
    site.getLinks().values().stream().forEach(link -> visitTopicLink(locale, link)); 
  }
  
  private void visitTopicLink(String locale, TopicLink link) {
    if(!Boolean.TRUE.equals(link.getWorkflow())) {
      return;
    }
    visitForm(locale, link);
  }
  
  private void visitForm(String locale, TopicLink link) {
    final var form = deployment.getSources().getDialob().stream()
        .filter(e -> link.getFormId().equals(e.getId()))
        .findFirst()
        .get();

    final var tag = dialobClient.findAllFormTags(form.getName()).stream()
        .filter(e -> e.getName().equals(link.getFormTag()))
        .findFirst();
    
    final var existingForm = dialobClient.findOneFormById(link.getFormId());
    
    
    // no tag and no form
    if(tag.isEmpty() && existingForm.isEmpty()) {
      final var created = createForm(form, link);
      if(created != null) {
        createTag(created, link);
      }
      return;
    }
    
    // no tag but form is present with same revision
    if(tag.isEmpty() && existingForm.isPresent() && existingForm.get().getRev().equals(form.getRev())) {
      createTag(form, link);
      return;
    }
    
    
    // no tag but form has different revision
    if(tag.isEmpty() && existingForm.isPresent() && !existingForm.get().getRev().equals(form.getRev())) {
      final var updated = updateForm(form, link);
      createTag(updated, link);
      return;
    }
    
    existingForm.ifPresent(existing -> logger.formUpToDate(existing, link));
  }
  
  
  private Form updateForm(Form form, TopicLink link) {
    try {
      final var updated = dialobClient.updateForm(form);
      logger.formUpdated(updated, link);
      return updated;
    } catch(Exception e) {
      addError();
      logger.failedToUpdateForm(form, link, e);
      return null;
    }
  }
  
  private Form createForm(Form form, TopicLink link) {
    try {
      final var created = dialobClient.createForm(form);
      logger.formCreated(created, link);
      return created;
    } catch(Exception e) {
      
      addError();
      logger.failedToCreateFormTag(form, link, e);
      return null;
    }
  }
  
  private void createTag(Form form, TopicLink link) {
    try {
      dialobClient.createTag(form.getName(), link.getFormTag());
      logger.formTagCreated(form, link);
    } catch(Exception e) {
      addError();
      logger.failedToCreateFormTag(form, link, e);
    }
  }
  
  private void addError() {
    errorIndex++;

  }
}
