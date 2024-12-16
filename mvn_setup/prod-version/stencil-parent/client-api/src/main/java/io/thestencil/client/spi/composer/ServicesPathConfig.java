package io.thestencil.client.spi.composer;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data @Builder
public class ServicesPathConfig {
  private final String servicePath;
  private final String migrationPath;
  private final String articlesPath;
  private final String pagesPath;
  private final String workflowsPath;
  private final String linksPath;
  private final String releasesPath;
  private final String localePath;
  private final String templatesPath;
  private final String versionPath;
}
