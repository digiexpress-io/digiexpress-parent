package io.digiexpress.eveli.client.web.resources;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import io.dialob.api.form.FormTag;
import io.digiexpress.eveli.client.api.DialobCommands.FormListItem;
import io.digiexpress.eveli.client.api.DialobCommands.FormTagResult;
import io.digiexpress.eveli.client.config.PortalConfigBean;

@RestController
@RequestMapping("/api/forms")
public class FormsController {
  
  private final PortalConfigBean appPathConfig;
  private final RestTemplate restTemplate;

  public FormsController(PortalConfigBean appPathConfig, RestTemplate restTemplate) {
    this.appPathConfig = appPathConfig;
    this.restTemplate = restTemplate;
  }
  
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, path="/tags")
  public ResponseEntity<List<FormTagResult>> allTags() {
    List<FormTagResult> tags = new ArrayList<>();
    FormListItem[] forms = getForms();
    for (var form : forms) {
      FormTag[] formTags = getTags(form.getId());
      for (var formTag : formTags) {
        FormTagResult result = new FormTagResult();
        result.setFormName(form.getId());
        result.setFormLabel(form.getMetadata().getLabel());
        result.setTagFormId(formTag.getFormId());
        result.setTagName(formTag.getName());
        tags.add(result);
      }
    };
    return ResponseEntity.status(HttpStatus.OK).body(tags);
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public List<FormListItem> allForms() {
    FormListItem[] forms = getForms();

    return Arrays.asList(forms);
  }

  private FormTag[] getTags(String id) {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(appPathConfig.getFormsUrl())
        .pathSegment(id)
        .pathSegment("tags")
        .buildAndExpand();
    return restTemplate.getForObject(uriComponents.toUriString(), FormTag[].class);
  }

  private FormListItem[] getForms() {
    UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(appPathConfig.getFormsUrl())
        .buildAndExpand();
    return restTemplate.getForObject(uriComponents.toUriString(), FormListItem[].class);
  }
}
