package io.resys.limaone.spi.pdf;

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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.limaone.program.ImmutablePdfResult;
import io.resys.limaone.program.Runtime;
import io.resys.limaone.program.Runtime.TagomiPdfRenderer;
import io.resys.limaone.program.TagomiProgram;
import io.resys.limaone.program.TagomiProgram.PdfStatus;
import io.resys.limaone.spi.pdf.TagomiPdfCommand.PdfCompilationException;
import io.resys.limaone.spi.pdf.entities.PdfRequest;
import io.resys.limaone.spi.pdf.entities.PdfRequest.PdfDataModule;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiPdfRendererImpl implements TagomiPdfRenderer {
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  @Override
  public Uni<TagomiProgram.PdfResult> render(
      List<TagomiProgram.LocalizedPrintout> localizedPrintouts,
      String serviceName,
      String orchestratorName,
      Runtime runtime, String locale, JsonObject props) {

    final var localizedPrintout = localizedPrintouts.stream()
        .filter(lp -> lp.getLocale().equalsIgnoreCase(locale))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No printout found for locale: " + locale));

    final List<PdfRequest.PdfTemplate> printoutPages = localizedPrintout.getPrintoutPages().stream()
        .map(t -> PdfRequest.PdfTemplate.builder().id(t.getId()).value(t.getValue()).build())
        .collect(java.util.stream.Collectors.toList());

    final JsonObject wrenchData = resolveWrenchData(orchestratorName, props, runtime);

    final var dataModules = new ArrayList<PdfDataModule>();
    dataModules.add(PdfDataModule.builder()
        .moduleName("wrench")
        .bodyName("flow")
        .bodyValue(wrenchData.mapTo(Map.class))
        .build());
    dataModules.add(PdfDataModule.builder()
        .moduleName("service")
        .bodyName("props")
        .bodyValue(props.mapTo(Map.class))
        .build());

    for (final var resource : localizedPrintout.getResources()) {
      dataModules.add(PdfDataModule.builder()
          .moduleName("resources")
          .bodyName(resource.getId())
          .bodyValue(Map.of("__base64__", resource.getContent() != null ? resource.getContent() : ""))
          .build());
    }

    final var request = PdfRequest.builder()
        .timestamp(OffsetDateTime.now())
        .mainTemplateId(serviceName)
        .dataModules(dataModules)
        .templates(printoutPages)
        .build();

    try {
      final var base64 = new TagomiPdfCommand(objectMapper, baseUrl).compilePdf(request);
      return Uni.createFrom().item(ImmutablePdfResult.builder()
          .status(PdfStatus.OK)
          .locale(locale)
          .name(serviceName)
          .bodyBase64(base64)
          .build());
    } catch (PdfCompilationException e) {
      return Uni.createFrom().item(ImmutablePdfResult.builder()
          .status(PdfStatus.ERROR)
          .statusMessage(e.getMessage())
          .locale(locale)
          .name(serviceName)
          .build());
    }
  }

  @SuppressWarnings("unchecked")
  private static JsonObject resolveWrenchData(String orchestratorName, JsonObject props, Runtime runtime) {
    if (StringUtils.isEmpty(orchestratorName)) {
      return new JsonObject();
    }
    final var flowOpt = runtime.getBundle().queryFlows().name(orchestratorName).findOne();
    if (flowOpt.isEmpty()) {
      return new JsonObject();
    }
    final var result = flowOpt.get().run(props.mapTo(Map.class)).andGetBody();
    return new JsonObject(new HashMap<>(result.getReturns()));
  }
}
