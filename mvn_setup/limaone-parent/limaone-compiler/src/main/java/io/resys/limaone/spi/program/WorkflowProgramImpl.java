package io.resys.limaone.spi.program;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.ProgramInput;
import io.resys.limaone.program.ProgramInput.Participant;
import io.resys.limaone.program.ProgramInput.ParticipantForm;
import io.resys.limaone.program.WorkflowProgram;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorkflowProgramImpl implements WorkflowProgram {
  private static final long serialVersionUID = -7526807947234023047L;
  private final io.resys.limaone.program.Runtime runtime;
  private final Map<String, String> localeAndLabel;
  private final Model<ArticleWorkflow> target;
  private final ArticleWorkflow_AST ast;
  private final ProgramStatus status;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  
  @Override
  public String getId() {
    return target.getId();
  }
  @Override
  public String getName() {
    return ast.getName();
  }
  @Override
  public Boolean getAssignable() {
    return Boolean.TRUE.equals(target.getBody().getAssignable());
  }

  @Override
  public String getFormId() {
    return target.getBody().getFormId();
  }

  @Override
  public String getFormName() {
    return target.getBody().getFormName();
  }

  @Override
  public String getFormTag() {
    return target.getBody().getFormTag();
  }

  @Override
  public Map<String, String> getLabels() {
    return localeAndLabel;
  }
  
  @Override
  public BodyType getType() {
    return BodyType.ARTICLE_WORKFLOW;
  }
  
  @Override
  public ProgramStatus getStatus() {
    return status;
  }

  @Override
  public List<Parameter> getHeaders() {
    return Collections.emptyList();
  }

  @Override
  public List<ModelError> getErrors() {
    return errors;
  }

  @Override
  public List<ProgramAssociation> getAssociations() {
    return associations;
  }

  @Override
  public WorkflowFormResult runForm(Participant identity, WorkflowProps programInput) {
    return new WorkflowFormExecutor(runtime, identity, programInput).walk(ast);
  }

  @Override
  public WorkflowFlowResult runFlow(ParticipantForm form, ProgramInput programInput) {
    return new WorkflowFlowExecutor(runtime, form, programInput).walk(ast);
  }

  @Override
  public List<String> getLocales() {
    return localeAndLabel.keySet().stream().toList();
  }
}
