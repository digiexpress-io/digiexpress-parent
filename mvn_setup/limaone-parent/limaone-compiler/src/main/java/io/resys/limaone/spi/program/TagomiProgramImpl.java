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
import java.util.stream.Collectors;

import io.resys.limaone.ast.Printout_AST;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.TagomiProgram;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;


public class TagomiProgramImpl implements TagomiProgram {

  private static final long serialVersionUID = 1207431853642101247L;

  private final Runtime runtime;
  private final String id;
  private final Printout_AST ast;
  private final ProgramStatus status;
  private final List<ModelError> errors;
  private final List<ProgramAssociation> associations;
  private final List<LocalizedPrintout> localizedPrintouts;
  private final List<String> locales;

  public TagomiProgramImpl(
      Runtime runtime,
      String id,
      Printout_AST ast,
      ProgramStatus status,
      List<ModelError> errors,
      List<ProgramAssociation> associations,
      List<LocalizedPrintout> localizedPrintouts) {

    this.runtime = runtime;
    this.id = id;
    this.ast = ast;
    this.status = status;
    this.errors = Collections.unmodifiableList(errors);
    this.associations = Collections.unmodifiableList(associations);
    this.localizedPrintouts = Collections.unmodifiableList(localizedPrintouts);
    this.locales = localizedPrintouts.stream()
        .map(LocalizedPrintout::getLocale)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override public String getId() { return id; }
  @Override public String getName() { return ast.getServiceName(); }
  @Override public BodyType getType() { return BodyType.PRINTOUT; }
  @Override public ProgramStatus getStatus() { return status; }
  @Override public List<String> getLocales() { return locales; }
  @Override public List<Parameter> getHeaders() { return Collections.emptyList(); }
  @Override public List<ModelError> getErrors() { return errors; }
  @Override public List<ProgramAssociation> getAssociations() { return associations; }

  @Override public String getServiceName() { return ast.getServiceName(); }
  @Override public String getOrchestratorName() { return ast.getOrchestratorName(); }
  @Override public List<LocalizedPrintout> getLocalizedPrintouts() { return localizedPrintouts; }

  @Override
  public Uni<PdfResult> run(String locale, JsonObject props) {
    return runtime.getProperties().getTagomiPdfRenderer().render(
        localizedPrintouts,
        ast.getServiceName(),
        ast.getOrchestratorName(),
        runtime, locale, props);
  }
}
