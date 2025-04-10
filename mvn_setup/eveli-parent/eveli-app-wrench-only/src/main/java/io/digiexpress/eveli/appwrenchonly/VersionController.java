package io.digiexpress.eveli.appwrenchonly;

import java.time.LocalDateTime;

/*-
 * #%L
 * eveli-app-gcloud
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

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;

@RestController
@RequestMapping("/worker/rest/api/version")
public class VersionController {

  @GetMapping
  public ResponseEntity<VersionInfo> getVersionInfo() {
    VersionInfo result = new VersionInfo();
    result.setVersion("wrench only demo");
    result.setBuilt(LocalDateTime.now().toString());
    return ResponseEntity.ok(result);
  }
  
  @Data
  public static class VersionInfo {
    private String version;
    private String built;
  }
}