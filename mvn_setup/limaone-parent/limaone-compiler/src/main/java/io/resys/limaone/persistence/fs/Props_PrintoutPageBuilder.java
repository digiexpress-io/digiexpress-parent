package io.resys.limaone.persistence.fs;

import java.util.Collections;

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

import io.resys.limaone.fs.ImmutablePrintoutPageProps;
import io.resys.limaone.fs.WorldFsProps.PrintoutPageProps;
import io.resys.limaone.model.PrintoutPage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_PrintoutPageBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public PrintoutPageProps build() {
    final PrintoutPage page = currentState.getBodyOfType(node);
    
    return ImmutablePrintoutPageProps.builder()
          .id(node.getObjectId())
          .type(node.getBodyType())
          .locked(false)
          .localeId(page.getLocaleId())
          .content(page.getContent())
          .serviceId(page.getServiceId())
          .templateIds(page.getPrintoutPageIds())
          .description(node.getDescription().map(e -> e.getText()).orElse(null))
          .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static PrintoutPageProps of(WorldFsState currentState, NodeAndBody node ) {
    return new Props_PrintoutPageBuilder(currentState, node).build();
  }

}
