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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.ProgramEnvir.ProgramWrapper;
import io.thestencil.client.api.MigrationBuilder.TopicLink;
import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class EveliDeploymentCompilerLogger {
  private final long start = System.currentTimeMillis();
  private final List<LogEvent> events = new ArrayList<>();
  private final List<LogEvent> errors = Collections.synchronizedList(new ArrayList<>());

  
  @Data @Builder
  private static class LogEvent {
    private final Map<String, String> props;
    private final LogEventType type;
  }

  private static enum LogEventType {
    COMPILING_DEPLOYMENT,
    
    STENCIL_CONTENT_MISSING,
    STENCIL_WORKFLOW_FORM_NAME_MISSING,
    STENCIL_WORKFLOW_FORM_TAG_MISSING,
    STENCIL_WORKFLOW_FORM_ID_MISSING,
    STENCIL_WORKFLOW_FORM_MISSING,
    STENCIL_WORKFLOW_FORM_LOCALE_MISSING,
    
    STENCIL_WORKFLOW_FLOW_NAME_MISSING,
    WRENCH_FLOW_MISSING,
    WRENCH_FLOW_BROKEN,
    
    DIALOB_FAILED_TO_CREATE_FORM_TAG,
    DIALOB_FAILED_TO_CREATE_FORM,
    DIALOB_FAILED_TO_UPDATE_FORM,
    
    
    DIALOB_FORM_UPDATED,
    WRENCH_FLOW_OK,
    DIALOB_FORM_CREATED,
    DIALOB_FORM_TAG_CREATED,
    DIALOB_FORM_OK,
    STENCIL_WK_OK
  }
  
  public EveliDeploymentCompilerLogger failedToUpdateForm(TopicLink link, Exception e) {
    final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
    errors.add(
      LogEvent.builder()
          .type(LogEventType.DIALOB_FAILED_TO_UPDATE_FORM)
          .props(Map.of(
              "text", "can't update dialob form",
              "form name", link.getFormName(),
              "form tag", link.getFormTag(),
              "wk value", link.getValue(),
              "stack", stack
              ))
          .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger failedToUpdateForm(Form form, TopicLink link, Exception e) {
    final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
    errors.add(
      LogEvent.builder()
          .type(LogEventType.DIALOB_FAILED_TO_UPDATE_FORM)
          .props(Map.of(
              "text", "can't update dialob form",
              "form id", form.getId(),
              "form name", link.getFormName(),
              "form tag", link.getFormTag(),
              "wk value", link.getValue(),
              "stack", stack
              ))
          .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger failedToCreateForm(Form form, TopicLink link, Exception e) {
    final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
    errors.add(
      LogEvent.builder()
          .type(LogEventType.DIALOB_FAILED_TO_CREATE_FORM)
          .props(Map.of(
              "text", "can't create dialob form",
              "form id", form.getId(),
              "form name", link.getFormName(),
              "form tag", link.getFormTag(),
              "wk value", link.getValue(),
              "stack", stack
              ))
          .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger failedToCreateFormTag(Form form, TopicLink link, Exception e) {
    final var stack = String.join(System.lineSeparator(), ExceptionUtils.getRootCauseStackTrace(e));
    errors.add(
      LogEvent.builder()
          .type(LogEventType.DIALOB_FAILED_TO_CREATE_FORM_TAG)
          .props(Map.of(
              "text", "can't create dialob form tag",
              "form id", form.getId(),
              "form name", link.getFormName(),
              "form tag", link.getFormTag(),
              "wk value", link.getValue(),
              "stack", stack
              ))
          .build()
    );
    
    return this;
  }
  
  public EveliDeploymentCompilerLogger formIdMissing(String locale, TopicLink link) {
    errors.add(
      LogEvent.builder()
          .type(LogEventType.STENCIL_WORKFLOW_FORM_ID_MISSING)
          .props(Map.of(
              "text", "form id is missing on workflow",
              "wk value", link.getValue(),
              "wk id", link.getId()
              ))
          .build()
    );
    
    return this;
  }
  public EveliDeploymentCompilerLogger formNameMissing(String locale, TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WORKFLOW_FORM_NAME_MISSING)
            .props(Map.of(
                "text", "form name is missing on workflow",
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
    );
    return this;
  }
  public EveliDeploymentCompilerLogger formTagMissing(String locale, TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WORKFLOW_FORM_TAG_MISSING)
            .props(Map.of(
                "text", "form tag is missing on workflow",
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
    );
    
    return this;
  }
  
  public EveliDeploymentCompilerLogger formMissing(String locale, TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WORKFLOW_FORM_MISSING)
            .props(Map.of(
                "text", "can't find dialob form",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
      );
    return this;
  }
  
  public EveliDeploymentCompilerLogger formLocaleMissing(Form form, String locale, TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WORKFLOW_FORM_LOCALE_MISSING)
            .props(Map.of(
                "text", "can't find dialob form locale that is configured in stencil",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "form locales", String.join(",", form.getMetadata().getLanguages()),
                "wk value", link.getValue(),
                "wk locale", locale,
                "wk id", link.getId()
                ))
            .build()
      );
    return this;
  }
  public EveliDeploymentCompilerLogger stencilMissing() {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_CONTENT_MISSING)
            .props(Map.of(
                "text", "No stencil content"
                ))
            .build()
      );
    return this;
  }  
  
  public EveliDeploymentCompilerLogger flowNameMissing(TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WORKFLOW_FLOW_NAME_MISSING)
            .props(Map.of(
                "text", "stencil workflow flow name is missing",
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
      );
    return this;
  }
  
  public EveliDeploymentCompilerLogger flowMissing(TopicLink link) {
    errors.add(
        LogEvent.builder()
            .type(LogEventType.WRENCH_FLOW_MISSING)
            .props(Map.of(
                "text", "can't find wrench flow",
                "flow name", link.getFlowName(),
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
      );
    return this;
  }
  public EveliDeploymentCompilerLogger flowBroken(TopicLink link, ProgramWrapper<AstFlow, FlowProgram> flow) {
    final var flowErrors = flow.getErrors().stream().map(e -> e.getId() + "/" + e.getMsg()).toList();
    errors.add(
        LogEvent.builder()
            .type(LogEventType.WRENCH_FLOW_BROKEN)
            .props(Map.of(
                "text", "broken wrench flow",
                "errors", String.join(",", flowErrors),
                "flow name", link.getFlowName(),
                "wk value", link.getValue(),
                "wk id", link.getId()
                ))
            .build()
    );
    return this;
  }  
  
  public EveliDeploymentCompilerLogger compiling(EveliDeployment deployment) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.COMPILING_DEPLOYMENT)
            .props(Map.of(
                "text", "compiling deployment",
                "deployment id", deployment.getId(),
                "name", deployment.getName(),
                "status", deployment.getStatus().name(),
                "startsAt", deployment.getStartsAt().toString(),
                "createdBy", deployment.getCreatedBy()
                ))
            .build()
    );
    return this;
  }
  public EveliDeploymentCompilerLogger formUpdated(Form form, TopicLink link) {
    
    events.add(
        LogEvent.builder()
            .type(LogEventType.DIALOB_FORM_UPDATED)
            .props(Map.of(
                "text", "dialob form tag created",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }  
  public EveliDeploymentCompilerLogger formCreated(Form form, TopicLink link) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.DIALOB_FORM_CREATED)
            .props(Map.of(
                "text", "dialob form created",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }

  public EveliDeploymentCompilerLogger formTagCreated(Form form, TopicLink link) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.DIALOB_FORM_TAG_CREATED)
            .props(Map.of(
                "text", "dialob form tag created",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger formUpToDate(Form form, TopicLink link) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.DIALOB_FORM_OK)
            .props(Map.of(
                "text", "dialob form OK",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger topicLinkFormOk(Form form, String locale, TopicLink link) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.STENCIL_WK_OK)
            .props(Map.of(
                "text", "stencil workflow OK",
                "form id", link.getId(),
                "form name", link.getFormName(),
                "form tag", link.getFormTag(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }
  
  public EveliDeploymentCompilerLogger topicLinkFlowOk(ProgramWrapper<AstFlow, FlowProgram> flow, TopicLink link) {
    events.add(
        LogEvent.builder()
            .type(LogEventType.WRENCH_FLOW_OK)
            .props(Map.of(
                "text", "wrench flow OK",
                "flow name", link.getFlowName(),
                "wk value", link.getValue()
                ))
            .build()
    );
    return this;
  }
  
  public JsonObject getErrors() {
   return JsonObject.of("errors", errors); 
  }
  
  public void info() {
    if(events.isEmpty()) {
      return;
    }
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator());
      
      event.props.entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
      
    }
    for(final var event : errors) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator());
      
      event.props.entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
    }
    
    final var cost = System.currentTimeMillis() - start;
    log.info("New version compiled, cost: {} millis, \r\n{}", cost, result.toString());
  }
  
  public void error() {
    if(events.isEmpty()) {
      return;
    }
    final var result = new StringBuilder("events:").append(System.lineSeparator());
    for(final var event : events) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator());
      
      event.props.entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
      
    }
    for(final var event : errors) {
      result
        .append("  - ").append(event.getType()).append(":").append(System.lineSeparator());
      
      event.props.entrySet().forEach(e -> 
        result.append("    ").append(e.getKey()).append(": ").append(e.getValue()).append(System.lineSeparator())
      );
    }
    final var cost = System.currentTimeMillis() - start;
    log.error("Failed to compiled new version, cost: {} millis, \r\n{}", cost, result.toString());
  }
}
