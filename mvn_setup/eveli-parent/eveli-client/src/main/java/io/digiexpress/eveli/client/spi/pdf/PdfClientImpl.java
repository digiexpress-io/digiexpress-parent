package io.digiexpress.eveli.client.spi.pdf;

/*-
 * #%L
 * eveli-client
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

import io.digiexpress.eveli.client.api.PdfClient;
import io.digiexpress.eveli.client.api.TaskClient;
import io.resys.limaone.spi.dialob.FormDb;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class PdfClientImpl implements PdfClient {
  private final TaskClient taskClient;
  private final FormDb formDb;
  private final io.resys.limaone.program.Runtime runtime;
  private final String serviceName;
  private final String defaultLocale;

  @Override
  public ProcessQuestionnairePdfBuilder pdfBuilder() {
    return new ProcessQuestionnairePdfBuilderImpl(taskClient, formDb, runtime, serviceName, defaultLocale);
  }
}
