package io.digiexpress.tagomi.spi;

/*-
 * #%L
 * tagomi-client
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.digiexpress.tagomi.rust.entities.PdfRequest.PdfDataModule;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.tagomi.api.TagomiClient.WorldDatasource;
import io.digiexpress.tagomi.api.entities.ImmutablePdf;
import io.digiexpress.tagomi.api.entities.ImmutablePdfEnvelope;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiProgram;
import io.digiexpress.tagomi.api.entities.TagomiWorld;
import io.digiexpress.tagomi.rust.TagomiPdfCommand;
import io.digiexpress.tagomi.rust.TagomiPdfCommand.PdfCompilationException;
import io.digiexpress.tagomi.rust.entities.PdfRequest;
import io.digiexpress.tagomi.rust.entities.PdfRequest.PdfTemplate;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiWorldImpl implements TagomiWorld {
  private final ObjectMapper objectMapper;
  private final TagomiContainer container;
  private final WorldDatasource datasource;
  private final RestTemplate restTemplate;
  private final String baseUrl;
  
  @Override
  public Map<String, List<TagomiProgram>> getProgramsByName() {
    return Collections.emptyMap();
  }
  
  @Override
  public PdfCompiler compiler() {
    return new PdfCompiler() {
      String locale;
      JsonObject props;
      
      @Override
      public PdfCompiler locale(String locale) {
        this.locale = locale;
        return this;
      }
      @Override
      public PdfCompiler inputJson(JsonObject inputJson) {
        this.props = inputJson;
        return this;
      }
      @SuppressWarnings("unchecked")
      @Override
      public Uni<PdfEnvelope> compile(String programIdOrName) {
        final var service = container.getServices().values().stream()
            .filter(e -> e.getId().equals(programIdOrName) || e.getServiceName().equals(programIdOrName))
            .findFirst().orElseThrow();
        
        final var targetLocale = container.getLocales().values().stream()
            .filter(l -> l.getLocaleCode().equalsIgnoreCase(locale) || l.getId().equals(locale))
            .findFirst().orElseThrow();
        
        final var templates = new ArrayList<PdfTemplate>();
        templates.add(PdfTemplate.builder()
            .id(service.getServiceName())
            .value(container.getTemplates().values().stream()
                .filter(t -> t.getServiceId().equals(service.getId()))
                .map(t -> t.getContent())
                .findFirst().orElseThrow()
            )
            .build());
        
                
        
        return datasource.get(service, this.props)
        .onItem().transform(resolved -> {
          final var dataModules = new ArrayList<PdfDataModule>();
          dataModules.add(PdfDataModule.builder()
              .moduleName("wrench")
              .bodyName("flow")
              .bodyValue(resolved.mapTo(Map.class))
              .build());

          dataModules.add(PdfDataModule.builder()
              .moduleName("service")
              .bodyName("props")
              .bodyValue(this.props.mapTo(Map.class))
              .build());
          
          return PdfRequest.builder()
              .timestamp(OffsetDateTime.now())
              .mainTemplateId(service.getServiceName())
              .dataModules(dataModules)
              .templates(templates)
              .build();
        })
        .onItem().transform(request -> {
          try {
            final var base64 = new TagomiPdfCommand(restTemplate, objectMapper, baseUrl).compilePdf(request);
            final var localisedName = service.getLabels().stream()
                .filter(l -> l.getLocale().equals(targetLocale.getLocaleCode()))
                .map(e -> e.getLabelValue())
                .findAny().orElse(null); 
            
            return ImmutablePdfEnvelope.builder()
                .status(TagomiPdfStatus.OK)
                .value(ImmutablePdf.builder()
                    .localisedName(localisedName)
                    .locale(locale)
                    .name(service.getServiceName())
                    .bodyBase64(base64)
                    .build())
                .build();
          } catch(PdfCompilationException e) {
            return  ImmutablePdfEnvelope.builder()
                .status(TagomiPdfStatus.ERROR)
                .statusMessage(e.getMessage())
                .build();
            
          }
        });

      }
    };
  }
}
