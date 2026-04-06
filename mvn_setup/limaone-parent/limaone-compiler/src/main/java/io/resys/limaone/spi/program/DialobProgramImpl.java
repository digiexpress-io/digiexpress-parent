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

import io.resys.limaone.ast.DialobForm_AST;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.DialobProgram;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DialobProgramImpl implements DialobProgram {
  private static final long serialVersionUID = -4564270222840631238L;
  private final io.resys.limaone.program.Runtime runtime;
  private final Model<DialobForm> target;
  private final DialobForm_AST ast;
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
  public BodyType getType() {
    return BodyType.DIALOB_FORM;
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
  public FormInstanceResult run(CreateFormInstanceInput props) {
    final var instance = runtime.getProperties().getFormDb().withTenant()
      .createFormInstance()
      .formId(ast.getForm().getId())
      .language(props.locale())
      .context(props.context())
      .build()
      .runSubscriptionOn(runtime.getProperties().getWorkerPool())
      .await().atMost(runtime.getProperties().getWorkerPoolMaxTimeout());
    
    return new FormInstanceResult(
        target.getBody().getFormName(), 
        target.getBody().getFormTagName(), 
        instance.getId());
  }
  @Override
  public List<String> getLocales() {
    return Collections.emptyList();
  }
}
