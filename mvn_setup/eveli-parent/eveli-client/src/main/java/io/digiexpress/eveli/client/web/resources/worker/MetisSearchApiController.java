package io.digiexpress.eveli.client.web.resources.worker;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.digiexpress.eveli.client.spi.metis.search.MetisLiveIndexTrigger;
import io.resys.metis.search.api.MetisSearchClient;
import io.resys.metis.search.api.MetisSearchIndexStatus;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/worker/rest/api/metis/search")
@RequiredArgsConstructor
public class MetisSearchApiController {

  private final MetisSearchClient search;
  private final MetisLiveIndexTrigger trigger;

  @GetMapping("/status")
  public Uni<MetisSearchIndexStatus> status() {
    return search.index().getIndexStatus();
  }

  @PostMapping("/reindex")
  public Uni<ResponseEntity<MetisSearchIndexStatus>> reindex(
      @RequestParam(name = "force", required = false, defaultValue = "false") boolean force,
      @RequestParam(name = "replace", required = false, defaultValue = "false") boolean replace) {

    return trigger.startNow(force, replace).onItem().transform(this::toResponse);
  }

  @PostMapping("/reindex/cancel")
  public Uni<ResponseEntity<MetisSearchIndexStatus>> cancel() {
    return search.index().cancelReindex().onItem().transform(this::toResponse);
  }

  private ResponseEntity<MetisSearchIndexStatus> toResponse(MetisSearchIndexStatus status) {
    return ResponseEntity
        .status(status.getAccepted() ? HttpStatus.ACCEPTED : HttpStatus.CONFLICT)
        .body(status);
  }
}
