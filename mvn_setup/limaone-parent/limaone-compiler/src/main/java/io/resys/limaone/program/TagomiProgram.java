package io.resys.limaone.program;

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

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


public interface TagomiProgram extends Program {

  String getServiceName();
  String getOrchestratorName();

  List<LocalizedPrintout> getLocalizedPrintouts();

  Uni<PdfResult> run(String locale, JsonObject props);


  @Value.Immutable
  @JsonSerialize(as = ImmutableLocalizedPrintout.class)
  @JsonDeserialize(as = ImmutableLocalizedPrintout.class)
  interface LocalizedPrintout {
    String getLocale();
    List<ResolvedPrintoutPage> getPrintoutPages();
    List<ResolvedResource> getResources();
  }


  @Value.Immutable
  @JsonSerialize(as = ImmutableResolvedPrintoutPage.class)
  @JsonDeserialize(as = ImmutableResolvedPrintoutPage.class)
  interface ResolvedPrintoutPage {
    String getId();
    String getValue();
  }


  @Value.Immutable
  @JsonSerialize(as = ImmutableResolvedResource.class)
  @JsonDeserialize(as = ImmutableResolvedResource.class)
  interface ResolvedResource {
    String getId();
    @Nullable String getContent();
    String getContentType();
  }


  @Value.Immutable
  @JsonSerialize(as = ImmutablePdfResult.class)
  @JsonDeserialize(as = ImmutablePdfResult.class)
  interface PdfResult extends ProgramResult {
    PdfStatus getStatus();
    @Nullable String getStatusMessage();
    @Nullable String getBodyBase64();
    String getLocale();
    String getName();
    @Nullable String getLocalisedName();
  }


  enum PdfStatus { OK, ERROR }
}
