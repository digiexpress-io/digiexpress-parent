package io.resys.limaone.spi.pdf.entities;

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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfRequest {
  @JsonProperty("main_template_id")
  private String mainTemplateId;
  private OffsetDateTime timestamp;
  private List<PdfTemplate> templates;
  @JsonProperty("data_modules")
  private List<PdfDataModule> dataModules;


  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PdfTemplate {
    private String id;
    private String value;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PdfDataModule {
    @JsonProperty("module_name")
    private String moduleName;

    @JsonProperty("body_name")
    private String bodyName;

    @JsonProperty("body_value")
    private Object bodyValue;
  }
}
