package io.resys.limaone.spi.compiler;

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
import java.util.stream.Collectors;

import io.resys.limaone.ast.AST_Parser;
import io.resys.limaone.ast.ImmutableHeaders_AST;
import io.resys.limaone.ast.ImmutablePrintout_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Printout;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.program.ImmutableLocalizedPrintout;
import io.resys.limaone.program.ImmutableResolvedResource;
import io.resys.limaone.program.ImmutableResolvedPrintoutPage;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.limaone.program.TagomiProgram.LocalizedPrintout;
import io.resys.limaone.spi.program.TagomiProgramImpl;
import io.smallrye.mutiny.tuples.Tuple2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class Compiler_Tagomi implements CompilableUnit {
  @SuppressWarnings("unused")
  private final AST_Parser parser;
  private final ModelWorld world;
  private final Model<Printout> printout;
  private Map<String, List<Model<PrintoutResource>>> printoutResourceByTemplateId;
  
  @Override
  public ArtifactLink compile(NewArtifact resolution) {
    this.printoutResourceByTemplateId = world.getPrintoutResourceByTemplateId();
    
    final var service = printout.getBody();

    final var enabledLocaleAndPages = world.getPrintoutPages().values().stream()
        .filter(p -> p.getBody().getServiceId().equals(printout.getId()))
        .map(page -> Tuple2.of(page, world.getLocales().get(page.getBody().getLocaleId())))
        .filter(p -> Boolean.TRUE.equals(p.getItem2().getBody().getEnabled()))
        .toList();

    final var localizedPrintouts = enabledLocaleAndPages.stream()
        .map(localeAndPage -> buildLocalizedPrintout(localeAndPage.getItem2(), localeAndPage.getItem1()))
        .toList();

    final var localeCodes = enabledLocaleAndPages.stream()
        .map(p -> p.getItem2().getBody().getValue())
        .collect(Collectors.toUnmodifiableList());

    final var ast = ImmutablePrintout_AST.builder()
        .bodyType(BodyType.PRINTOUT)
        .name(service.getServiceName())
        .hash(printout.getBodyHash())
        .headers(ImmutableHeaders_AST.builder().build())
        .serviceName(service.getServiceName())
        .orchestratorName(service.getOrchestratorName())
        .localeCodes(localeCodes)
        .build();

    resolution.ast(ast).id(printout.getId()).name(service.getServiceName()).build();

    final var resolvedLocales = Collections.unmodifiableList(localizedPrintouts);

    return new ArtifactLink() {
      @Override
      public Simple_AST getAst() {
        return ast;
      }
      @Override
      public RuntimeLink accept(Artifact artifact) {
        return (runtime) -> new TagomiProgramImpl(
            runtime,
            printout.getId(), ast,
            artifact.getErrors().isEmpty() ? artifact.getProgramStatus() : ProgramStatus.ERROR,
            artifact.getErrors(),
            artifact.getAssociations(),
            resolvedLocales);
      }
    };
  }

  private LocalizedPrintout buildLocalizedPrintout(Model<Locale> localeModel, Model<PrintoutPage> pageModel) {
    final var page = pageModel.getBody();
    final var builder = ImmutableLocalizedPrintout.builder()
      .locale(localeModel.getBody().getValue())
      .addPrintoutPages(ImmutableResolvedPrintoutPage.builder()
        .id(printout.getBody().getServiceName())
        .value(page.getContent())
        .build());

    addResourcesForPage(builder, pageModel.getId());

    for (final var pageId : page.getPrintoutPageIds()) {
      final var depPageModel = world.getPrintoutPages().get(pageId);
      if(depPageModel == null) {
        log.error("Can't find printout: {} page by id: {}", printout.getBody().getServiceName(), pageId);
        continue;
      }
      final var depPage = depPageModel.getBody();
      final var depService = world.getPrintouts().get(depPage.getServiceId());
      final var depLocale = world.getLocales().get(depPage.getLocaleId());
      final var depName = resolveTemplateName(depService, depLocale);

      builder.addPrintoutPages(ImmutableResolvedPrintoutPage.builder()
          .id(depName)
          .value(depPage.getContent())
          .build());

      addResourcesForPage(builder, pageId);
    }

    return builder.build();
  }

  private void addResourcesForPage(ImmutableLocalizedPrintout.Builder builder, String pageId) {
    for (final var resourceModel : printoutResourceByTemplateId.getOrDefault(pageId, Collections.emptyList())) {
      final var res = resourceModel.getBody();
      if (res.getContentType() != null && res.getContentType().startsWith("text/")) {
        builder.addPrintoutPages(ImmutableResolvedPrintoutPage.builder()
            .id(res.getResourceName())
            .value(res.getContent() != null ? res.getContent() : "")
            .build());
      } else {
        builder.addResources(ImmutableResolvedResource.builder()
            .id(res.getResourceName())
            .content(res.getContent())
            .contentType(res.getContentType())
            .build());
      }
    }
  }

  private static String resolveTemplateName(Model<Printout> service, Model<Locale> locale) {
    final var serviceName = service != null ? service.getBody().getServiceName() : "unknown";
    if (locale != null) {
      return serviceName + " - " + locale.getBody().getValue();
    }
    return serviceName;
  }
}
